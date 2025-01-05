package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.booleans.BooleanCollection
import it.unimi.dsi.fastutil.booleans.BooleanList

@SurfNettyPacket("boolean_array_response", PacketFlow.BIDIRECTIONAL)
class BooleanArrayResponse(val value: BooleanArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(BooleanArrayResponse::write, ::BooleanArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readBooleanArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeBooleanArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<BooleanArrayResponse>.respond(value: BooleanArray) =
    respond(BooleanArrayResponse(value))

fun RespondingNettyPacket<BooleanArrayResponse>.respond(value: BooleanCollection) =
    respond(value.toBooleanArray())