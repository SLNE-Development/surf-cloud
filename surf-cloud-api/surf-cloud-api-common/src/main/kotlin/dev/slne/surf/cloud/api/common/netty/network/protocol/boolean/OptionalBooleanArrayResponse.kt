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

typealias OptionalBooleanArrayResponsePacket = RespondingNettyPacket<OptionalBooleanArrayResponse>

@SurfNettyPacket("optional_boolean_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalBooleanArrayResponse(override val value: BooleanArray?) : ResponseNettyPacket(),
    CommonResponseType<BooleanArray?> {

    companion object : CommonResponseTypeFactory<OptionalBooleanArrayResponse, BooleanArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalBooleanArrayResponse::write, ::OptionalBooleanArrayResponse)

        override fun create(value: BooleanArray?): OptionalBooleanArrayResponse {
            return OptionalBooleanArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readBooleanArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeBooleanArray(value) }
    }

    operator fun component1() = value
}

fun OptionalBooleanArrayResponsePacket.respond(value: BooleanCollection?) =
    respond(value?.toBooleanArray())