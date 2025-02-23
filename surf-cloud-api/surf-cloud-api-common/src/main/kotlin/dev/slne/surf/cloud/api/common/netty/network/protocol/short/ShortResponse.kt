package dev.slne.surf.cloud.api.common.netty.network.protocol.short

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias ShortResponsePacket = RespondingNettyPacket<ShortResponse>

@SurfNettyPacket("short_response", PacketFlow.BIDIRECTIONAL)
class ShortResponse(override val value: Short) : ResponseNettyPacket(), CommonResponseType<Short> {
    companion object : CommonResponseTypeFactory<ShortResponse, Short> {
        val STREAM_CODEC = packetCodec(ShortResponse::write, ::ShortResponse)
        override fun create(value: Short): ShortResponse {
            return ShortResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readShort())

    private fun write(buf: SurfByteBuf) {
        buf.writeShort(value.toInt())
    }

    operator fun component1() = value
}