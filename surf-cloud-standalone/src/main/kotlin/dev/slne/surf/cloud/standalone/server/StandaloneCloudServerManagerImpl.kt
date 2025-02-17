package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundBatchUpdateServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundRegisterServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundUnregisterServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RequestOfflineDisplayNamePacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ProxyServerAutoregistration
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import java.util.*

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl : CommonCloudServerManagerImpl<ServerCommonCloudServer>(),
    ServerCloudServerManager {
    private val server by lazy { bean<NettyServerImpl>() }

    suspend fun getCommonStandaloneServerByUid(uid: Long) =
        retrieveServerById(uid) as? CommonStandaloneServer

    override suspend fun registerServer(cloudServer: ServerCommonCloudServer) {
        super.registerServer(cloudServer)
        broadcast(
            ClientboundRegisterServerPacket(
                cloudServer.uid,
                cloudServer is ServerProxyCloudServer,
                cloudServer.group,
                cloudServer.name
            )
        )

        cloudServer.connection.send(ClientboundBatchUpdateServer(retrieveAllServers()))

        if (standaloneConfig.useSingleProxySetup) {
            if (cloudServer is StandaloneProxyCloudServerImpl) {
                ProxyServerAutoregistration.setProxy(cloudServer)
            } else {
                ProxyServerAutoregistration.registerClient(cloudServer as StandaloneCloudServerImpl)
            }
        }
    }

    override suspend fun unregisterServer(uid: Long) {
        super.unregisterServer(uid)
        broadcast(ClientboundUnregisterServerPacket(uid))

        if (standaloneConfig.useSingleProxySetup) {
            if (getCommonStandaloneServerByUid(uid) is StandaloneProxyCloudServerImpl) {
                ProxyServerAutoregistration.clearProxy()
            }
        }
    }

    private fun broadcast(packet: NettyPacket) {
        server.connection.broadcast(packet)
    }

    suspend fun requestOfflineDisplayName(uuid: UUID): Component? {
        if (standaloneConfig.useSingleProxySetup) { // easy, we just ask the proxy
            return RequestOfflineDisplayNamePacket(uuid).fireAndAwaitOrThrowUrgent(singleProxyServer().connection).displayName
        } else {
            // here we play the lottery until we get a response or all servers have been asked
            val servers = retrieveAllServers()

            // the chance that a proxy response with a display name is very high,
            // so let's ask them first
            val proxies = servers.filterIsInstance<StandaloneProxyCloudServerImpl>()
            for (proxy in proxies) {
                val (name) = RequestOfflineDisplayNamePacket(uuid).fireAndAwaitOrThrowUrgent(proxy.connection)
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

    private suspend fun singleProxyServer() =
        serversMutex.withLock { servers.values.single { it is StandaloneProxyCloudServerImpl } }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl