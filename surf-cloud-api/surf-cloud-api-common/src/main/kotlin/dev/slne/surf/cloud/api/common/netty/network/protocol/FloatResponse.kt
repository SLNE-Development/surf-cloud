package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("float_response", PacketFlow.BIDIRECTIONAL)
class FloatResponse(val value: Float) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(FloatResponse::write, ::FloatResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readFloat())

    private fun write(buf: SurfByteBuf) {
        buf.writeFloat(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<FloatResponse>.respond(value: Float) =
    respond(FloatResponse(value))