package dev.slne.surf.cloud.api.common.netty.network.protocol.short

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.shorts.ShortCollection

typealias ShortArrayResponsePacket = RespondingNettyPacket<ShortArrayResponse>

@SurfNettyPacket("short_array_response", PacketFlow.BIDIRECTIONAL)
class ShortArrayResponse(override val value: ShortArray) : ResponseNettyPacket(),
    CommonResponseType<ShortArray> {
    companion object : CommonResponseTypeFactory<ShortArrayResponse, ShortArray> {
        val STREAM_CODEC = packetCodec(ShortArrayResponse::write, ::ShortArrayResponse)
        override fun create(value: ShortArray): ShortArrayResponse {
            return ShortArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readShortArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeShortArray(value)
    }

    operator fun component1() = value
}

fun ShortArrayResponsePacket.respond(value: ShortCollection) = respond(value.toShortArray())