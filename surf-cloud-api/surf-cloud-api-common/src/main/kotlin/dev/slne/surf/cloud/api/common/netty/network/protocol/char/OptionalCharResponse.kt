package dev.slne.surf.cloud.api.common.netty.network.protocol.char

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalCharResponsePacket = RespondingNettyPacket<OptionalCharResponse>

@SurfNettyPacket("optional_char_response", PacketFlow.BIDIRECTIONAL)
class OptionalCharResponse(override val value: Char?) : ResponseNettyPacket(),
    CommonResponseType<Char?> {
    companion object : CommonResponseTypeFactory<OptionalCharResponse, Char?> {
        val STREAM_CODEC = packetCodec(OptionalCharResponse::write, ::OptionalCharResponse)
        override fun create(value: Char?): OptionalCharResponse {
            return OptionalCharResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableChar())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}