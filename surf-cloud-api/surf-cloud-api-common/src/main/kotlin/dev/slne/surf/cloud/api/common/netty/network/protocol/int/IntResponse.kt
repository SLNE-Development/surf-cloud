package dev.slne.surf.cloud.api.common.netty.network.protocol.int

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias IntResponsePacket = RespondingNettyPacket<IntResponse>

@SurfNettyPacket("int_response", PacketFlow.BIDIRECTIONAL)
class IntResponse(override val value: Int) : ResponseNettyPacket(), CommonResponseType<Int> {
    companion object : CommonResponseTypeFactory<IntResponse, Int> {
        val STREAM_CODEC = packetCodec(IntResponse::write, ::IntResponse)
        override fun create(value: Int): IntResponse {
            return IntResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readVarInt())

    private fun write(buf: SurfByteBuf) {
        buf.writeVarInt(value)
    }

    operator fun component1() = value
}