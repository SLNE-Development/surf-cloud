package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("double_response", PacketFlow.BIDIRECTIONAL)
class DoubleResponse(val value: Double) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(DoubleResponse::write, ::DoubleResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readDouble())

    private fun write(buf: SurfByteBuf) {
        buf.writeDouble(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<DoubleResponse>.respond(value: Double) =
    respond(DoubleResponse(value))