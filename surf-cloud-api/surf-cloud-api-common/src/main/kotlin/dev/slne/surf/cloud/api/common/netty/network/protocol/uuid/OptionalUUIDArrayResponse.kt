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

typealias OptionalUUIDArrayResponsePacket = RespondingNettyPacket<OptionalUUIDArrayResponse>

@SurfNettyPacket("optional_uuid_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalUUIDArrayResponse(override val value: Array<UUID>?) : ResponseNettyPacket(),
    CommonResponseType<Array<UUID>?> {
    companion object : CommonResponseTypeFactory<OptionalUUIDArrayResponse, Array<UUID>?> {
        val STREAM_CODEC =
            packetCodec(OptionalUUIDArrayResponse::write, ::OptionalUUIDArrayResponse)

        override fun create(value: Array<UUID>?): OptionalUUIDArrayResponse {
            return OptionalUUIDArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readUuidArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeUuidArray(value) }
    }

    operator fun component1() = value
}

fun OptionalUUIDArrayResponsePacket.respond(value: Collection<UUID>?) =
    respond(value?.toTypedArray())