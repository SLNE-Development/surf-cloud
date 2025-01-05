package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("int_response", PacketFlow.BIDIRECTIONAL)
class IntResponse(val value: Int) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(IntResponse::write, ::IntResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readVarInt())

    private fun write(buf: SurfByteBuf) {
        buf.writeVarInt(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<IntResponse>.respond(value: Int) =
    respond(IntResponse(value))