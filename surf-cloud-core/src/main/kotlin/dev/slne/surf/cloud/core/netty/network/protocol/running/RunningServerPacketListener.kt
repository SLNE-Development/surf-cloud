package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener

interface RunningServerPacketListener: ServerboundPacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handlePacket(packet: NettyPacket)
}