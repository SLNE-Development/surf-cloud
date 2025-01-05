package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.UUID

@SurfNettyPacket("uuid_array_response", PacketFlow.BIDIRECTIONAL)
class UUIDArrayResponse(val value: Array<UUID>) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(UUIDArrayResponse::write, ::UUIDArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUuidArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeUuidArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<UUIDArrayResponse>.respond(value: Array<UUID>) =
    respond(UUIDArrayResponse(value))

fun RespondingNettyPacket<UUIDArrayResponse>.respond(value: Collection<UUID>) =
    respond(UUIDArrayResponse(value.toTypedArray()))