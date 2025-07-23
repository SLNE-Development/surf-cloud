package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.netty.network.protocol.awaitOrThrowUrgent
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.emptyObjectList
import dev.slne.surf.cloud.api.common.util.freeze
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundRegisterServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundUnregisterServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RequestOfflineDisplayNamePacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.event.server.CloudServerRegisteredEvent
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.Unmodifiable
import java.util.*

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl : CommonCloudServerManagerImpl<ServerCommonCloudServer>(),
    ServerCloudServerManager {

    private val log = logger()
    private val server by lazy { bean<NettyServerImpl>() }

    fun getCommonStandaloneServerByUid(uid: Long) =
        retrieveServerById(uid) as? CommonStandaloneServer

    fun getPingData() = serverCache.asMap().values.associate { it.name to it.connection.latency }

    override fun registerServer(server: ServerCommonCloudServer) {
        super.registerServer(server)
        broadcast(
            ClientboundRegisterServerPacket(
                server.uid,
                server is ServerProxyCloudServer,
                (server as? CloudServer)?.lobby ?: false,
                server.group,
                server.name,
                server.playAddress,
            )
        )

        try {
            CloudServerRegisteredEvent(this, server as CommonCloudServerImpl).postAndForget()
        } catch (e: Exception) {
            log.atWarning()
                .log(
                    "Failed to post CloudServerRegisteredEvent for server ${server.uid}",
                    e
                )
        }
    }

    override fun unregisterServer(uid: Long) = super.unregisterServer(uid).also {
        broadcast(ClientboundUnregisterServerPacket(uid))
    }

    override fun broadcast(packet: NettyPacket) {
        server.connection.broadcast(packet)
    }

    suspend fun requestOfflineDisplayName(uuid: UUID): Component? {
        if (standaloneConfig.useSingleProxySetup) { // easy, we just ask the proxy
            return RequestOfflineDisplayNamePacket(uuid).awaitOrThrowUrgent(singleProxyServer().connection)
        } else {
            // here we play the lottery until we get a response or all servers have been asked
            val servers = retrieveAllServers()

            // the chance that a proxy response with a display name is very high,
            // so let's ask them first
            val proxies = servers.filterIsInstance<StandaloneProxyCloudServerImpl>()
            for (proxy in proxies) {
                val name =
                    RequestOfflineDisplayNamePacket(uuid).awaitOrThrowUrgent(proxy.connection)
                if (name != null) {
                    return name
                }
            }

            // if no proxy responded, we ask the other servers
            for (server in servers) {
                if (server !is StandaloneProxyCloudServerImpl) {
                    val (name) = RequestOfflineDisplayNamePacket(uuid).fireAndAwaitOrThrowUrgent(
                        server.connection
                    )
                    if (name != null) {
                        return name
                    }
                }
            }

            // I guess no one knows the name
            return null
        }
    }

    override suspend fun pullPlayersToGroup(
        group: String,
        players: Collection<CloudPlayer>
    ): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>> {
        val toConnect = players
            .filterNot { it.isInGroup(group) }
            .filterIsInstance<StandaloneCloudPlayerImpl>()

        if (toConnect.isEmpty()) {
            return emptyObjectList()
        }

        val results = mutableObjectListOf<Pair<CloudPlayer, ConnectionResultEnum>>()
        val serversInGroup = retrieveServersByCategory(group)
            .filterIsInstance<StandaloneCloudServerImpl>()
            .toMutableList()

        if (serversInGroup.isEmpty()) {
            toConnect.forEach { player ->
                results += player to ConnectionResultEnum.SERVER_NOT_FOUND("group:$group")
            }

            return results.freeze()
        }

        val queue = ArrayDeque(toConnect)

        while (queue.isNotEmpty() && serversInGroup.isNotEmpty()) {
            serversInGroup.sortByDescending { server ->
                val maxPlayers = server.maxPlayerCount
                val expected = server.expectedPlayers
                maxPlayers - expected
            }

            val bestServer = serversInGroup.first()
            val freeSlots = bestServer.maxPlayerCount - bestServer.expectedPlayers

            if (freeSlots <= 0) break

            val subset = mutableObjectListOf<CloudPlayer>()
            repeat(freeSlots) {
                if (queue.isNotEmpty()) {
                    subset += queue.removeFirst()
                } else {
                    return@repeat
                }
            }

            val pullResult = bestServer.pullPlayers(subset)
            results += pullResult
        }

        while (queue.isNotEmpty()) {
            val leftoverPlayer = queue.removeFirst()
            results += leftoverPlayer to ConnectionResultEnum.SERVER_FULL
        }

        return results.freeze()
    }

    @Suppress("UNCHECKED_CAST")
    override fun retrieveProxies(): ObjectCollection<StandaloneProxyCloudServerImpl> {
        return super.retrieveProxies() as ObjectCollection<StandaloneProxyCloudServerImpl>
    }

    @Suppress("UNCHECKED_CAST")
    override fun retrieveServers(): ObjectCollection<StandaloneCloudServerImpl> {
        return super.retrieveServers() as ObjectCollection<StandaloneCloudServerImpl>
    }

    private fun singleProxyServer() =
        serverCache.asMap().values.single { it is StandaloneProxyCloudServerImpl }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl