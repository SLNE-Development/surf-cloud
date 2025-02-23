package dev.slne.surf.cloud.api.common.netty.network.protocol.boolean

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

typealias BooleanResponsePacket = RespondingNettyPacket<BooleanResponse>

@SurfNettyPacket("boolean_response", PacketFlow.BIDIRECTIONAL)
class BooleanResponse(override val value: Boolean) : ResponseNettyPacket(),
    CommonResponseType<Boolean> {
    companion object : CommonResponseTypeFactory<BooleanResponse, Boolean> {
        val STREAM_CODEC = packetCodec(BooleanResponse::write, ::BooleanResponse)
        override fun create(value: Boolean): BooleanResponse {
            return BooleanResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readBoolean())

    private fun write(buf: SurfByteBuf) {
        buf.writeBoolean(value)
    }

    operator fun component1() = value
}