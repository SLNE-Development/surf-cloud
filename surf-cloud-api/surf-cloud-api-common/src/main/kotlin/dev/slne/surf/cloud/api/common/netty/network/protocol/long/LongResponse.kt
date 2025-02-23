package dev.slne.surf.cloud.api.common.netty.network.protocol.long

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias LongResponsePacket = RespondingNettyPacket<LongResponse>

@SurfNettyPacket("long_response", PacketFlow.BIDIRECTIONAL)
class LongResponse(override val value: Long) : ResponseNettyPacket(), CommonResponseType<Long> {
    companion object : CommonResponseTypeFactory<LongResponse, Long> {
        val STREAM_CODEC = packetCodec(LongResponse::write, ::LongResponse)
        override fun create(value: Long): LongResponse {
            return LongResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readLong())

    private fun write(buf: SurfByteBuf) {
        buf.writeLong(value)
    }

    operator fun component1() = value
}