package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

/**
 * Response to the clientbound packet
 * ([dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundPingPacket])
 * with the same id.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_PONG_PACKET, PacketFlow.SERVERBOUND)
class ServerboundPongPacket : NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(ServerboundPongPacket::write, ::ServerboundPongPacket)
    }

    val pingId: Long

    constructor(pingId: Long) {
        this.pingId = pingId
    }

    private constructor(buffer: SurfByteBuf) {
        pingId = buffer.readLong()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeLong(pingId)
    }
}