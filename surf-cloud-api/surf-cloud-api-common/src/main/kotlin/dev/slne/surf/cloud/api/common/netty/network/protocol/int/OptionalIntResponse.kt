package dev.slne.surf.cloud.api.common.netty.network.protocol.int

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalIntResponsePacket = RespondingNettyPacket<OptionalIntResponse>

@SurfNettyPacket("optional_int_response", PacketFlow.BIDIRECTIONAL)
class OptionalIntResponse(override val value: Int?) : ResponseNettyPacket(),
    CommonResponseType<Int?> {
    companion object : CommonResponseTypeFactory<OptionalIntResponse, Int?> {
        val STREAM_CODEC = packetCodec(OptionalIntResponse::write, ::OptionalIntResponse)
        override fun create(value: Int?): OptionalIntResponse {
            return OptionalIntResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableInt())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}