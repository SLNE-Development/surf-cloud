package dev.slne.surf.cloud.api.common.netty.network.protocol.uuid

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

typealias UUIDArrayResponsePacket = RespondingNettyPacket<UUIDArrayResponse>

@SurfNettyPacket("uuid_array_response", PacketFlow.BIDIRECTIONAL)
class UUIDArrayResponse(override val value: Array<UUID>) : ResponseNettyPacket(),
    CommonResponseType<Array<UUID>> {
    companion object : CommonResponseTypeFactory<UUIDArrayResponse, Array<UUID>> {
        val STREAM_CODEC = packetCodec(UUIDArrayResponse::write, ::UUIDArrayResponse)
        override fun create(value: Array<UUID>): UUIDArrayResponse {
            return UUIDArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUuidArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeUuidArray(value)
    }

    operator fun component1() = value
}

fun UUIDArrayResponsePacket.respond(value: Collection<UUID>) = respond(value.toTypedArray())