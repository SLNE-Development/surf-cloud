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

typealias OptionalUUIDResponsePacket = RespondingNettyPacket<OptionalUUIDResponse>

@SurfNettyPacket("optional_uuid_response", PacketFlow.BIDIRECTIONAL)
class OptionalUUIDResponse(override val value: UUID?) : ResponseNettyPacket(),
    CommonResponseType<UUID?> {
    companion object : CommonResponseTypeFactory<OptionalUUIDResponse, UUID?> {
        val STREAM_CODEC = packetCodec(OptionalUUIDResponse::write, ::OptionalUUIDResponse)
        override fun create(value: UUID?): OptionalUUIDResponse {
            return OptionalUUIDResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableUuid())

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value)
    }

    operator fun component1() = value
}