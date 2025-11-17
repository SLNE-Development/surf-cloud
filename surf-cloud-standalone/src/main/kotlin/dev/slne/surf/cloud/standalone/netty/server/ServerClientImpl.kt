package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.cloud.standalone.netty.server.network.ServerRunningPacketListenerImpl
import java.net.InetSocketAddress

class ServerClientImpl(
    val nettyServer: NettyServerImpl,
    serverCategory: String,
    serverName: String,
    val lobbyServer: Boolean,
) : CommonNettyClientImpl(serverCategory, serverName) {

    override lateinit var playAddress: InetSocketAddress

    private var _listener: ServerRunningPacketListenerImpl? = null
    val listener get() = _listener ?: error("listener not yet set")

    override val velocitySecret: ByteArray
        get() = ProxySecretHolder.currentSecret()

    fun initListener(listener: ServerRunningPacketListenerImpl) {
        _listener = listener
    }

    override fun broadcast(packets: List<NettyPacket>) {
        nettyServer.connection.broadcast(packets)
    }

    companion object {
        fun fromPacket(server: NettyServerImpl, packet: ServerboundLoginStartPacket) =
            ServerClientImpl(
                server,
                packet.serverCategory,
                packet.serverName,
                packet.lobby
            )
    }
}