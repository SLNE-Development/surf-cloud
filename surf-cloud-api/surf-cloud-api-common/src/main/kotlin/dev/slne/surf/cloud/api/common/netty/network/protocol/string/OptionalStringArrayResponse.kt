package dev.slne.surf.cloud.api.common.netty.network.protocol.string

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias OptionalStringArrayResponsePacket = RespondingNettyPacket<OptionalStringArrayResponse>

@SurfNettyPacket("optional_string_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalStringArrayResponse(override val value: Array<String>?) : ResponseNettyPacket(),
    CommonResponseType<Array<String>?> {
    companion object : CommonResponseTypeFactory<OptionalStringArrayResponse, Array<String>?> {
        val STREAM_CODEC =
            packetCodec(OptionalStringArrayResponse::write, ::OptionalStringArrayResponse)

        override fun create(value: Array<String>?): OptionalStringArrayResponse {
            return OptionalStringArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readStringArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, v -> buf.writeStringArray(v) }
    }

    operator fun component1() = value
}

fun OptionalStringArrayResponsePacket.respond(value: Collection<String>) =
    respond(value.toTypedArray())