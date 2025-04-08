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

typealias StringArrayResponsePacket = RespondingNettyPacket<StringArrayResponse>

@SurfNettyPacket("string_array_response", PacketFlow.BIDIRECTIONAL)
class StringArrayResponse(override val value: Array<String>) : ResponseNettyPacket(),
    CommonResponseType<Array<String>> {
    companion object : CommonResponseTypeFactory<StringArrayResponse, Array<String>> {
        val STREAM_CODEC = packetCodec(StringArrayResponse::write, ::StringArrayResponse)
        override fun create(value: Array<String>): StringArrayResponse {
            return StringArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readStringArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeStringArray(value)
    }

    operator fun component1() = value
}

fun StringArrayResponsePacket.respond(value: Collection<String>) = respond(value.toTypedArray())