package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.chars.CharCollection

@SurfNettyPacket("char_array_response", PacketFlow.BIDIRECTIONAL)
class CharArrayResponse(val value: CharArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(CharArrayResponse::write, ::CharArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readCharArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeCharArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<CharArrayResponse>.respond(value: CharArray) =
    respond(CharArrayResponse(value))

fun RespondingNettyPacket<CharArrayResponse>.respond(value: CharCollection) =
    respond(value.toCharArray())