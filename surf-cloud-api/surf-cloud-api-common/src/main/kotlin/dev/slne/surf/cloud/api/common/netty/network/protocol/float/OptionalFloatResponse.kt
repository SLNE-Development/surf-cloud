package dev.slne.surf.cloud.api.common.netty.network.protocol.float

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalFloatResponsePacket = RespondingNettyPacket<OptionalFloatResponse>

@SurfNettyPacket("optional_float_response", PacketFlow.BIDIRECTIONAL)
class OptionalFloatResponse(override val value: Float?) : ResponseNettyPacket(),
    CommonResponseType<Float?> {
    companion object : CommonResponseTypeFactory<OptionalFloatResponse, Float?> {
        val STREAM_CODEC = packetCodec(OptionalFloatResponse::write, ::OptionalFloatResponse)
        override fun create(value: Float?): OptionalFloatResponse {
            return OptionalFloatResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableFloat())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}