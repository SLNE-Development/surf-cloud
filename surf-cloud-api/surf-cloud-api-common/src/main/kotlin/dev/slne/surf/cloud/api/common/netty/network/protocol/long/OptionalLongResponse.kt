package dev.slne.surf.cloud.api.common.netty.network.protocol.long

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalLongResponsePacket = RespondingNettyPacket<OptionalLongResponse>

@SurfNettyPacket("optional_long_response", PacketFlow.BIDIRECTIONAL)
class OptionalLongResponse(override val value: Long?) : ResponseNettyPacket(),
    CommonResponseType<Long?> {
    companion object : CommonResponseTypeFactory<OptionalLongResponse, Long?> {
        val STREAM_CODEC = packetCodec(OptionalLongResponse::write, ::OptionalLongResponse)
        override fun create(value: Long?): OptionalLongResponse {
            return OptionalLongResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableLong())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}