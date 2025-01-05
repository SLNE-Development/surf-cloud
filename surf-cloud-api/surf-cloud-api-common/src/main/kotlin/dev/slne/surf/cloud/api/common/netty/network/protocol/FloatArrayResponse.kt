package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.floats.FloatCollection

@SurfNettyPacket("float_array_response", PacketFlow.BIDIRECTIONAL)
class FloatArrayResponse(val value: FloatArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(FloatArrayResponse::write, ::FloatArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readFloatArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeFloatArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<FloatArrayResponse>.respond(value: FloatArray) =
    respond(FloatArrayResponse(value))

fun RespondingNettyPacket<FloatArrayResponse>.respond(value: FloatCollection) =
    respond(value.toFloatArray())