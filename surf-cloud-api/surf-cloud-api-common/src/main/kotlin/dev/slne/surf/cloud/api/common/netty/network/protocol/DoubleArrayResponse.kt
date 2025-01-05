package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.doubles.DoubleCollection

@SurfNettyPacket("boolean_array_response", PacketFlow.BIDIRECTIONAL)
class DoubleArrayResponse(val value: DoubleArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(DoubleArrayResponse::write, ::DoubleArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readDoubleArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeDoubleArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<DoubleArrayResponse>.respond(value: DoubleArray) =
    respond(DoubleArrayResponse(value))

fun RespondingNettyPacket<DoubleArrayResponse>.respond(value: DoubleCollection) =
    respond(value.toDoubleArray())