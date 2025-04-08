package dev.slne.surf.cloud.api.common.netty.network.protocol.double

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.doubles.DoubleCollection

typealias OptionalDoubleArrayResponsePacket = RespondingNettyPacket<OptionalDoubleArrayResponse>

@SurfNettyPacket("optional_double_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalDoubleArrayResponse(override val value: DoubleArray?) : ResponseNettyPacket(),
    CommonResponseType<DoubleArray?> {
    companion object : CommonResponseTypeFactory<OptionalDoubleArrayResponse, DoubleArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalDoubleArrayResponse::write, ::OptionalDoubleArrayResponse)

        override fun create(value: DoubleArray?): OptionalDoubleArrayResponse {
            return OptionalDoubleArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readDoubleArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, v -> buf.writeDoubleArray(v) }
    }

    operator fun component1() = value
}

fun OptionalDoubleArrayResponsePacket.respond(value: DoubleCollection?) =
    respond(value?.toDoubleArray())