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

typealias OptionalByteArrayResponsePacket = RespondingNettyPacket<OptionalByteArrayResponse>

@SurfNettyPacket("optional_byte_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalByteArrayResponse(override val value: ByteArray?) : ResponseNettyPacket(),
    CommonResponseType<ByteArray?> {
    companion object : CommonResponseTypeFactory<OptionalByteArrayResponse, ByteArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalByteArrayResponse::write, ::OptionalByteArrayResponse)

        override fun create(value: ByteArray?): OptionalByteArrayResponse {
            return OptionalByteArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readByteArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeByteArray(value) }
    }

    operator fun component1() = value
}

fun OptionalByteArrayResponsePacket.respond(value: ByteCollection?) =
    respond(value?.toByteArray())