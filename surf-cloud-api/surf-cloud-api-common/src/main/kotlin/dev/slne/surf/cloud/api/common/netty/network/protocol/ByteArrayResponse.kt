package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.bytes.ByteCollection
import it.unimi.dsi.fastutil.bytes.ByteList

@SurfNettyPacket("byte_array_response", PacketFlow.BIDIRECTIONAL)
class ByteArrayResponse(val value: ByteArray) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(ByteArrayResponse::write, ::ByteArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readByteArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeByteArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<ByteArrayResponse>.respond(value: ByteArray) =
    respond(ByteArrayResponse(value))

fun RespondingNettyPacket<ByteArrayResponse>.respond(value: ByteCollection) = respond(value.toByteArray())