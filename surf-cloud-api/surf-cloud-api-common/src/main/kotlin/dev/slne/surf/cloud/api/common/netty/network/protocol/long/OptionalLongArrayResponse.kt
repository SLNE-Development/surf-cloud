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

typealias OptionalLongArrayResponsePacket = RespondingNettyPacket<OptionalLongArrayResponse>

@SurfNettyPacket("optional_long_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalLongArrayResponse(override val value: LongArray?) : ResponseNettyPacket(),
    CommonResponseType<LongArray?> {
    companion object : CommonResponseTypeFactory<OptionalLongArrayResponse, LongArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalLongArrayResponse::write, ::OptionalLongArrayResponse)

        override fun create(value: LongArray?): OptionalLongArrayResponse {
            return OptionalLongArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readLongArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeLongArray(value) }
    }

    operator fun component1() = value
}

fun OptionalLongArrayResponsePacket.respond(value: LongCollection?) = respond(value?.toLongArray())