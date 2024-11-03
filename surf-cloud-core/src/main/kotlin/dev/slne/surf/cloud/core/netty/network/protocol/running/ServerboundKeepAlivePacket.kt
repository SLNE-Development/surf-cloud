package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

/**
 * The server will frequently send out a keep-alive (see
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundKeepAlivePacket]),
 * each containing a random ID. The client must respond with the same packet.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_KEEP_ALIVE_PACKET, PacketFlow.SERVERBOUND)
class ServerboundKeepAlivePacket: NettyPacket {
    companion object{
        @JvmStatic
        val STREAM_CODEC = packetCodec(ServerboundKeepAlivePacket::write, ::ServerboundKeepAlivePacket)
    }

    val keepAliveId: Long

    constructor(keepAliveId: Long) {
        this.keepAliveId = keepAliveId
    }

    private constructor(buffer: SurfByteBuf) {
        keepAliveId = buffer.readLong()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeLong(keepAliveId)
    }
}