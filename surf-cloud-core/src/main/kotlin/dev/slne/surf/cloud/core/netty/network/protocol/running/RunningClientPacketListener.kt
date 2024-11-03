package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.netty.network.TickablePacketListener

interface RunningClientPacketListener: ClientboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handleKeepAlive(packet: ClientboundKeepAlivePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)
}