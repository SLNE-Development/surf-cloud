package dev.slne.surf.cloud.api.common.netty.network.protocol.float

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.floats.FloatCollection

typealias FloatArrayResponsePacket = RespondingNettyPacket<FloatArrayResponse>

@SurfNettyPacket("float_array_response", PacketFlow.BIDIRECTIONAL)
class FloatArrayResponse(override val value: FloatArray) : ResponseNettyPacket(),
    CommonResponseType<FloatArray> {
    companion object : CommonResponseTypeFactory<FloatArrayResponse, FloatArray> {
        val STREAM_CODEC = packetCodec(FloatArrayResponse::write, ::FloatArrayResponse)
        override fun create(value: FloatArray): FloatArrayResponse {
            return FloatArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readFloatArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeFloatArray(value)
    }

    operator fun component1() = value
}

fun FloatArrayResponsePacket.respond(value: FloatCollection) = respond(value.toFloatArray())