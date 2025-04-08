package dev.slne.surf.cloud.api.common.netty.network.protocol.int

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.ints.IntCollection

typealias OptionalIntArrayResponsePacket = RespondingNettyPacket<OptionalIntArrayResponse>

@SurfNettyPacket("optional_int_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalIntArrayResponse(override val value: IntArray?) : ResponseNettyPacket(), CommonResponseType<IntArray?> {
    companion object : CommonResponseTypeFactory<OptionalIntArrayResponse, IntArray?> {
        val STREAM_CODEC = packetCodec(OptionalIntArrayResponse::write, ::OptionalIntArrayResponse)
        override fun create(value: IntArray?): OptionalIntArrayResponse {
            return OptionalIntArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readIntArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeIntArray(value) }
    }

    operator fun component1() = value
}

fun OptionalIntArrayResponsePacket.respond(value: IntCollection?) = respond(value?.toIntArray())