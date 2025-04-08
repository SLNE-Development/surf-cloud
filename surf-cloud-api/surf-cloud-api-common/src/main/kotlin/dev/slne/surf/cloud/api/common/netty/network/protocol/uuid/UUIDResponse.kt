package dev.slne.surf.cloud.api.common.netty.network.protocol.uuid

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

typealias UUIDResponsePacket = RespondingNettyPacket<UUIDResponse>

@SurfNettyPacket("uuid_response", PacketFlow.BIDIRECTIONAL)
class UUIDResponse(override val value: UUID) : ResponseNettyPacket(), CommonResponseType<UUID> {
    companion object : CommonResponseTypeFactory<UUIDResponse, UUID> {
        val STREAM_CODEC = packetCodec(UUIDResponse::write, ::UUIDResponse)
        override fun create(value: UUID): UUIDResponse {
            return UUIDResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUuid())

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(value)
    }

    operator fun component1() = value
}