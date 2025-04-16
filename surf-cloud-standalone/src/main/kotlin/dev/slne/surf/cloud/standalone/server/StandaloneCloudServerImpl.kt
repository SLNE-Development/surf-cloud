package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.coroutines.CloudServerCleanupScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTransferPlayerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTriggerShutdownPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse.Status
import dev.slne.surf.cloud.core.common.server.AbstractCloudServer
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.server.queue.SingleServerQueue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StandaloneCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: ConnectionImpl,
) : AbstractCloudServer(uid, group, name), ServerCloudServer,
    CommonStandaloneServer by CommonStandaloneServerImpl() {

    init {
        wrapper = this
        startCleanupTask()
    }

    val connectingPlayers = mutableObjectSetOf<StandaloneCloudPlayerImpl>()
    val queue = SingleServerQueue(this)

    override val expectedPlayers: Int
        get() = currentPlayerCount + connectingPlayers.size

    private fun startCleanupTask() = CloudServerCleanupScope.launch {
        while (isActive) {
            connectingPlayers.removeIf { !it.connecting || it.connectingToServer != this }
            delay(5.seconds)
        }
    }

    suspend fun pullPlayer(player: CloudPlayer): ConnectionResultEnum {
        require(player is StandaloneCloudPlayerImpl) { "Player must be StandaloneCloudPlayerImpl" }
        if (player.connecting) return ConnectionResultEnum.CONNECTION_IN_PROGRESS
        player.connecting = true
        player.connectingToServer = this
        connectingPlayers.add(player)

        val proxy = player.proxyServer
        if (proxy != null) {
            return pullPlayerThroughProxy(player, proxy).also {
                connectingPlayers.remove(player)
                player.connecting = false
                player.connectingToServer = null
            }
        }

        TODO("Use transfer packet")
    }

    private suspend fun pullPlayerThroughProxy(
        player: StandaloneCloudPlayerImpl,
        proxy: StandaloneProxyCloudServerImpl
    ): ConnectionResultEnum {
        val result = ClientboundTransferPlayerPacket(player.uuid, proxy.connection.virtualHost)
            .fireAndAwait(proxy.connection, Duration.INFINITE)
            ?: error("Failed to pull player through proxy")

        return when (result.status) {
            Status.SUCCESS -> ConnectionResultEnum.SUCCESS
            Status.ALREADY_CONNECTED -> ConnectionResultEnum.ALREADY_CONNECTED
            Status.CONNECTION_IN_PROGRESS -> ConnectionResultEnum.CONNECTION_IN_PROGRESS
            Status.CONNECTION_CANCELLED -> ConnectionResultEnum.CONNECTION_CANCELLED
            Status.SERVER_DISCONNECTED -> ConnectionResultEnum.SERVER_DISCONNECTED
        }
    }

    override fun shutdown() {
        connection.send(ClientboundTriggerShutdownPacket)
    }
}

fun ServerCommonCloudServer?.asStandaloneServer() = this as? StandaloneCloudServerImpl