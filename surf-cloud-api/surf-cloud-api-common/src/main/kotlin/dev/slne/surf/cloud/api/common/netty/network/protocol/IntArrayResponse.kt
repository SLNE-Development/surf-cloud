package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.ints.IntCollection

@SurfNettyPacket("int_array_response", PacketFlow.BIDIRECTIONAL)
class IntArrayResponse(val value: IntArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(IntArrayResponse::write, ::IntArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readIntArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeIntArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<IntArrayResponse>.respond(value: IntArray) =
    respond(IntArrayResponse(value))

fun RespondingNettyPacket<IntArrayResponse>.respond(value: IntCollection) =
    respond(value.toIntArray())