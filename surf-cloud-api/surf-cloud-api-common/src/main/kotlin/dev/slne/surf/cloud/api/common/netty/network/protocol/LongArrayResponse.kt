package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.longs.LongCollection

@SurfNettyPacket("long_array_response", PacketFlow.BIDIRECTIONAL)
class LongArrayResponse(val value: LongArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(LongArrayResponse::write, ::LongArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readLongArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeLongArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<LongArrayResponse>.respond(value: LongArray) =
    respond(LongArrayResponse(value))

fun RespondingNettyPacket<LongArrayResponse>.respond(value: LongCollection) =
    respond(LongArrayResponse(value.toLongArray()))