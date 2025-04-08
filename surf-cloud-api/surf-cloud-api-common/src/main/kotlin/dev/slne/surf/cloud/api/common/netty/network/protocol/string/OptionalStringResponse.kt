package dev.slne.surf.cloud.api.common.netty.network.protocol.string

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalStringResponsePacket = RespondingNettyPacket<OptionalStringResponse>

@SurfNettyPacket("optional_string_response", PacketFlow.BIDIRECTIONAL)
class OptionalStringResponse(override val value: String?) : ResponseNettyPacket(),
    CommonResponseType<String?> {
    companion object : CommonResponseTypeFactory<OptionalStringResponse, String?> {
        val STREAM_CODEC = packetCodec(OptionalStringResponse::write, ::OptionalStringResponse)
        override fun create(value: String?): OptionalStringResponse {
            return OptionalStringResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableString())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}