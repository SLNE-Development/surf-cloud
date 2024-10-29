package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.protocol.login.ServerLoginPacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginStartPacket
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

class ServerLoginPacketListenerImpl(val connection: Connection) : ServerLoginPacketListener {
    private var client: ServerClientImpl? = null

    override fun handleLoginStart(packet: ServerboundLoginStartPacket) {
        this.client = ServerClientImpl(packet.serverId, packet.serverCategory)
        // more logic here

        connection.send(ClientboundLoginFinishedPacket)
    }

    override suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket) {
        connection.setupOutboundProtocol(RunningProtocols.CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf))
        connection.setupInboundProtocol(
            RunningProtocols.SERVERBOUND_TEMPLATE.bind(::SurfByteBuf),
            ServerRunningPacketListenerImpl(connection)
        )

        // TODO add to client tracker
    }

    override fun onDisconnect(reason: String) {

    }
}