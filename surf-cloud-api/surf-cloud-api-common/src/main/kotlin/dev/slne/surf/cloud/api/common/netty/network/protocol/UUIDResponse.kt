package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket("uuid_response", PacketFlow.BIDIRECTIONAL)
class UUIDResponse(val value: UUID) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(UUIDResponse::write, ::UUIDResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUuid())

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<UUIDResponse>.respond(value: UUID) =
    respond(UUIDResponse(value))