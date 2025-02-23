package dev.slne.surf.cloud.api.common.netty.network.protocol.boolean

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalBooleanResponsePacket = RespondingNettyPacket<OptionalBooleanResponse>

@SurfNettyPacket("optional_boolean_response", PacketFlow.BIDIRECTIONAL)
class OptionalBooleanResponse(override val value: Boolean?) : ResponseNettyPacket(),
    CommonResponseType<Boolean?> {
    companion object : CommonResponseTypeFactory<OptionalBooleanResponse, Boolean?> {
        val STREAM_CODEC = packetCodec(OptionalBooleanResponse::write, ::OptionalBooleanResponse)
        override fun create(value: Boolean?): OptionalBooleanResponse {
            return OptionalBooleanResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableBoolean())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}