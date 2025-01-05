package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("char_response", PacketFlow.BIDIRECTIONAL)
class CharResponse(val value: Char) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(CharResponse::write, ::CharResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readChar())

    private fun write(buf: SurfByteBuf) {
        buf.writeChar(value.code)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<CharResponse>.respond(value: Char) =
    respond(CharResponse(value))