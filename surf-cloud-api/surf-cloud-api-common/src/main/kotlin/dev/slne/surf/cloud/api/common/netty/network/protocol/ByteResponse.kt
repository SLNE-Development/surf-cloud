package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("byte_response", PacketFlow.BIDIRECTIONAL)
class ByteResponse(val value: Byte) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(ByteResponse::write, ::ByteResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readByte())

    private fun write(buf: SurfByteBuf) {
        buf.writeByte(value.toInt())
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<ByteResponse>.respond(value: Byte) =
    respond(ByteResponse(value))