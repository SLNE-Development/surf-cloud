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

typealias DoubleArrayResponsePacket = RespondingNettyPacket<DoubleArrayResponse>

@SurfNettyPacket("double_array_response", PacketFlow.BIDIRECTIONAL)
class DoubleArrayResponse(override val value: DoubleArray) : ResponseNettyPacket(),
    CommonResponseType<DoubleArray> {
    companion object : CommonResponseTypeFactory<DoubleArrayResponse, DoubleArray> {
        val STREAM_CODEC = packetCodec(DoubleArrayResponse::write, ::DoubleArrayResponse)
        override fun create(value: DoubleArray): DoubleArrayResponse {
            return DoubleArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readDoubleArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeDoubleArray(value)
    }

    operator fun component1() = value
}

fun DoubleArrayResponsePacket.respond(value: DoubleCollection) =
    respond(value.toDoubleArray())