package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.cloud.standalone.netty.server.network.ServerRunningPacketListenerImpl

class ServerClientImpl(
    val server: NettyServerImpl,
    serverId: Long,
    serverCategory: String,
    serverName: String
) : CommonNettyClientImpl(serverId, serverCategory, serverName) {

    private var _listener: ServerRunningPacketListenerImpl? = null
        set(value) {
            field = value
            if (value != null) {
                initConnection(value.connection)
            }
        }
    val listener get() = _listener ?: error("listener not yet set")

    fun initListener(listener: ServerRunningPacketListenerImpl) {
        _listener = listener
    }

    override fun broadcast(packets: List<NettyPacket>) {
        server.connection.broadcast(packets)
    }

    companion object {
        fun fromPacket(server: NettyServerImpl, packet: ServerboundLoginStartPacket) =
            ServerClientImpl(server, packet.serverId, packet.serverCategory, packet.serverName)
    }
}