package dev.slne.surf.cloud.api.common.netty.network.protocol.byte

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias ByteResponsePacket = RespondingNettyPacket<ByteResponse>

@SurfNettyPacket("byte_response", PacketFlow.BIDIRECTIONAL)
class ByteResponse(override val value: Byte) : ResponseNettyPacket(), CommonResponseType<Byte> {
    companion object : CommonResponseTypeFactory<ByteResponse, Byte> {
        val STREAM_CODEC = packetCodec(ByteResponse::write, ::ByteResponse)

        override fun create(value: Byte): ByteResponse {
            return ByteResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readByte())

    private fun write(buf: SurfByteBuf) {
        buf.writeByte(value.toInt())
    }

    operator fun component1() = value
}