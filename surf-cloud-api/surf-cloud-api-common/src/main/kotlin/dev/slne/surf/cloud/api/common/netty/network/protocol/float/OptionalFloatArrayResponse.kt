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

typealias OptionalFloatArrayResponsePacket = RespondingNettyPacket<OptionalFloatArrayResponse>

@SurfNettyPacket("optional_float_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalFloatArrayResponse(override val value: FloatArray?) : ResponseNettyPacket(),
    CommonResponseType<FloatArray?> {
    companion object : CommonResponseTypeFactory<OptionalFloatArrayResponse, FloatArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalFloatArrayResponse::write, ::OptionalFloatArrayResponse)

        override fun create(value: FloatArray?): OptionalFloatArrayResponse {
            return OptionalFloatArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readFloatArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeFloatArray(value) }
    }

    operator fun component1() = value
}

fun OptionalFloatArrayResponsePacket.respond(value: FloatCollection?) =
    respond(value?.toFloatArray())