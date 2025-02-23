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

typealias IntArrayResponsePacket = RespondingNettyPacket<IntArrayResponse>

@SurfNettyPacket("int_array_response", PacketFlow.BIDIRECTIONAL)
class IntArrayResponse(override val value: IntArray) : ResponseNettyPacket(),
    CommonResponseType<IntArray> {
    companion object : CommonResponseTypeFactory<IntArrayResponse, IntArray> {
        val STREAM_CODEC = packetCodec(IntArrayResponse::write, ::IntArrayResponse)
        override fun create(value: IntArray): IntArrayResponse {
            return IntArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readIntArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeIntArray(value)
    }

    operator fun component1() = value
}

fun IntArrayResponsePacket.respond(value: IntCollection) = respond(value.toIntArray())