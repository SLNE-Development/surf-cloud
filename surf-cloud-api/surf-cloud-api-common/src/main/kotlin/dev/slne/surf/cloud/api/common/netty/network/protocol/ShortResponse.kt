package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("short_response", PacketFlow.BIDIRECTIONAL)
class ShortResponse(val value: Short) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(ShortResponse::write, ::ShortResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readShort())

    private fun write(buf: SurfByteBuf) {
        buf.writeShort(value.toInt())
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<ShortResponse>.respond(value: Short) =
    respond(ShortResponse(value))
