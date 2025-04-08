package dev.slne.surf.cloud.api.common.netty.network.protocol.float

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias FloatResponsePacket = RespondingNettyPacket<FloatResponse>

@SurfNettyPacket("float_response", PacketFlow.BIDIRECTIONAL)
class FloatResponse(override val value: Float) : ResponseNettyPacket(), CommonResponseType<Float> {
    companion object : CommonResponseTypeFactory<FloatResponse, Float> {
        val STREAM_CODEC = packetCodec(FloatResponse::write, ::FloatResponse)
        override fun create(value: Float): FloatResponse {
            return FloatResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readFloat())

    private fun write(buf: SurfByteBuf) {
        buf.writeFloat(value)
    }

    operator fun component1() = value
}