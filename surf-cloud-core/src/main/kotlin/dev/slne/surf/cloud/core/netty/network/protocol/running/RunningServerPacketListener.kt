package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.netty.network.TickablePacketListener
import dev.slne.surf.cloud.core.netty.protocol.packets.ServerboundBroadcastPacket

interface RunningServerPacketListener: ServerboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket)

    fun handleBroadcastPacket(packet: ServerboundBroadcastPacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

    fun handlePacket(packet: NettyPacket)
}