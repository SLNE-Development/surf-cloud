package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("long_response", PacketFlow.BIDIRECTIONAL)
class LongResponse(val value: Long) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(LongResponse::write, ::LongResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readLong())

    private fun write(buf: SurfByteBuf) {
        buf.writeLong(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<LongResponse>.respond(value: Long) =
    respond(LongResponse(value))