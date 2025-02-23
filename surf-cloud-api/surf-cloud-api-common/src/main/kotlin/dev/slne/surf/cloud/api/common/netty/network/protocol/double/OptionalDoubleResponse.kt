package dev.slne.surf.cloud.api.common.netty.network.protocol.double

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalDoubleResponsePacket = RespondingNettyPacket<OptionalDoubleResponse>

@SurfNettyPacket("optional_double_response", PacketFlow.BIDIRECTIONAL)
class OptionalDoubleResponse(override val value: Double?) : ResponseNettyPacket(),
    CommonResponseType<Double?> {
    companion object {
        val STREAM_CODEC = packetCodec(OptionalDoubleResponse::write, ::OptionalDoubleResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableDouble())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}