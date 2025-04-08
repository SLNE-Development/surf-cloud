package dev.slne.surf.cloud.api.common.netty.network.protocol.char

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias CharResponsePacket = RespondingNettyPacket<CharResponse>

@SurfNettyPacket("char_response", PacketFlow.BIDIRECTIONAL)
class CharResponse(override val value: Char) : ResponseNettyPacket(), CommonResponseType<Char> {
    companion object : CommonResponseTypeFactory<CharResponse, Char> {
        val STREAM_CODEC = packetCodec(CharResponse::write, ::CharResponse)
        override fun create(value: Char): CharResponse {
            return CharResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readChar())

    private fun write(buf: SurfByteBuf) {
        buf.writeChar(value.code)
    }

    operator fun component1() = value
}