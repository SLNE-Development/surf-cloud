package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener

interface RunningServerPacketListener: ServerboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handleBundlePacket(packet: ServerboundBundlePacket)

    suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

    fun handlePacket(packet: NettyPacket)
}