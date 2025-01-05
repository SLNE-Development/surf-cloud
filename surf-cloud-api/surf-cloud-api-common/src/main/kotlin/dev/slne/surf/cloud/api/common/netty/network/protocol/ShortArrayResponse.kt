package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.shorts.ShortCollection

@SurfNettyPacket("short_array_response", PacketFlow.BIDIRECTIONAL)
class ShortArrayResponse(val value: ShortArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(ShortArrayResponse::write, ::ShortArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readShortArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeShortArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<ShortArrayResponse>.respond(value: ShortArray) =
    respond(ShortArrayResponse(value))

fun RespondingNettyPacket<ShortArrayResponse>.respond(value: ShortCollection) =
    respond(ShortArrayResponse(value.toShortArray()))