package dev.slne.surf.cloud.api.common.netty.network.protocol.byte

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalByteResponsePacket = RespondingNettyPacket<OptionalByteResponse>

@SurfNettyPacket("optional_byte_response", PacketFlow.BIDIRECTIONAL)
class OptionalByteResponse(override val value: Byte?) : ResponseNettyPacket(),
    CommonResponseType<Byte?> {
    companion object : CommonResponseTypeFactory<OptionalByteResponse, Byte?> {
        val STREAM_CODEC = packetCodec(OptionalByteResponse::write, ::OptionalByteResponse)

        override fun create(value: Byte?): OptionalByteResponse {
            return OptionalByteResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableByte())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}