package dev.slne.surf.cloud.api.common.netty.network.protocol.byte

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.bytes.ByteCollection

typealias ByteArrayResponsePacket = RespondingNettyPacket<ByteArrayResponse>

@SurfNettyPacket("byte_array_response", PacketFlow.BIDIRECTIONAL)
class ByteArrayResponse(override val value: ByteArray) : ResponseNettyPacket(),
    CommonResponseType<ByteArray> {
    companion object : CommonResponseTypeFactory<ByteArrayResponse, ByteArray> {
        val STREAM_CODEC = packetCodec(ByteArrayResponse::write, ::ByteArrayResponse)
        override fun create(value: ByteArray): ByteArrayResponse {
            return ByteArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readByteArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeByteArray(value)
    }

    operator fun component1() = value
}

fun ByteArrayResponsePacket.respond(value: ByteCollection) = respond(value.toByteArray())