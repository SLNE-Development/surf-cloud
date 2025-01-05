package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("string_response", PacketFlow.BIDIRECTIONAL)
class StringResponse(val value: String) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(StringResponse::write, ::StringResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUtf())

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<StringResponse>.respond(value: String) =
    respond(StringResponse(value))