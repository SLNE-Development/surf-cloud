package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("boolean_response", PacketFlow.BIDIRECTIONAL)
class BooleanResponse(val value: Boolean) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(BooleanResponse::write, ::BooleanResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readBoolean())

    private fun write(buf: SurfByteBuf) {
        buf.writeBoolean(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<BooleanResponse>.respond(value: Boolean) =
    respond(BooleanResponse(value))