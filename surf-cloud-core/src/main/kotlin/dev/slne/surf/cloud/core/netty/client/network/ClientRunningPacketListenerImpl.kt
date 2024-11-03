package dev.slne.surf.cloud.core.netty.client.network

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.netty.network.protocol.running.*

class ClientRunningPacketListenerImpl(val connection: Connection) : CommonTickablePacketListener(),
    RunningClientPacketListener {
    private val log = logger()

    override suspend fun tick0() {

    }

    override fun handleKeepAlive(packet: ClientboundKeepAlivePacket) {
        send(ServerboundKeepAlivePacket(packet.keepAliveId))
    }

    override fun handlePing(packet: ClientboundPingPacket) {
        send(ServerboundPongPacket(packet.pingId))
    }

    override fun handleDisconnect(packet: ClientboundDisconnectPacket) {
        connection.disconnect(packet.details)
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("Client disconnected with reason: ${details.reason}")
        // TODO: shutdown server if not already shutting down
    }

    fun send(packet: NettyPacket) {
        connection.send(packet)
    }
}