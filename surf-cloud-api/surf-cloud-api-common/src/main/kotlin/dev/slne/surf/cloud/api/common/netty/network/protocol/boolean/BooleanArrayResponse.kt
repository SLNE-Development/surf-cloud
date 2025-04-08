package dev.slne.surf.cloud.api.common.netty.network.protocol.boolean

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.booleans.BooleanCollection

typealias BooleanArrayResponsePacket = RespondingNettyPacket<BooleanArrayResponse>

@SurfNettyPacket("boolean_array_response", PacketFlow.BIDIRECTIONAL)
class BooleanArrayResponse(override val value: BooleanArray) : ResponseNettyPacket(),
    CommonResponseType<BooleanArray> {

    companion object : CommonResponseTypeFactory<BooleanArrayResponse, BooleanArray> {
        val STREAM_CODEC = packetCodec(BooleanArrayResponse::write, ::BooleanArrayResponse)
        override fun create(value: BooleanArray): BooleanArrayResponse {
            return BooleanArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readBooleanArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeBooleanArray(value)
    }

    operator fun component1() = value
}

fun BooleanArrayResponsePacket.respond(value: BooleanCollection) =
    respond(value.toBooleanArray())