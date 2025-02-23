package dev.slne.surf.cloud.api.common.netty.network.protocol.long

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.longs.LongCollection

typealias LongArrayResponsePacket = RespondingNettyPacket<LongArrayResponse>

@SurfNettyPacket("long_array_response", PacketFlow.BIDIRECTIONAL)
class LongArrayResponse(override val value: LongArray) : ResponseNettyPacket(),
    CommonResponseType<LongArray> {
    companion object : CommonResponseTypeFactory<LongArrayResponse, LongArray> {
        val STREAM_CODEC = packetCodec(LongArrayResponse::write, ::LongArrayResponse)
        override fun create(value: LongArray): LongArrayResponse {
            return LongArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readLongArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeLongArray(value)
    }

    operator fun component1() = value
}

fun LongArrayResponsePacket.respond(value: LongCollection) = respond(value.toLongArray())