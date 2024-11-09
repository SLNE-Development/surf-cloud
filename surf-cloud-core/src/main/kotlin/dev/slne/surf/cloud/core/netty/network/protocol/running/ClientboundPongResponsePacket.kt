package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.CLIENTBOUND_PING_REQUEST_RESPONSE_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundPongResponsePacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundPongResponsePacket::write, ::ClientboundPongResponsePacket)
    }

    val time: Long

    constructor(time: Long) {
        this.time = time
    }

    private constructor(buf: SurfByteBuf) {
        time = buf.readLong()
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeLong(time)
    }
}