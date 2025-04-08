package dev.slne.surf.cloud.api.common.netty.network.protocol.short

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalShortResponsePacket = RespondingNettyPacket<OptionalShortResponse>

@SurfNettyPacket("optional_short_response", PacketFlow.BIDIRECTIONAL)
class OptionalShortResponse(override val value: Short?) : ResponseNettyPacket(),
    CommonResponseType<Short?> {
    companion object : CommonResponseTypeFactory<OptionalShortResponse, Short?> {
        val STREAM_CODEC = packetCodec(OptionalShortResponse::write, ::OptionalShortResponse)
        override fun create(value: Short?): OptionalShortResponse {
            return OptionalShortResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readShort() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}