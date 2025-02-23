package dev.slne.surf.cloud.api.common.netty.network.protocol.string

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias StringResponsePacket = RespondingNettyPacket<StringResponse>

@SurfNettyPacket("string_response", PacketFlow.BIDIRECTIONAL)
class StringResponse(override val value: String) : ResponseNettyPacket(),
    CommonResponseType<String> {
    companion object : CommonResponseTypeFactory<StringResponse, String> {
        val STREAM_CODEC = packetCodec(StringResponse::write, ::StringResponse)
        override fun create(value: String): StringResponse {
            return StringResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUtf())

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(value)
    }

    operator fun component1() = value
}