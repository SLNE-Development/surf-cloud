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

typealias OptionalShortArrayResponsePacket = RespondingNettyPacket<OptionalShortArrayResponse>

@SurfNettyPacket("optional_short_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalShortArrayResponse(override val value: ShortArray?) : ResponseNettyPacket(),
    CommonResponseType<ShortArray?> {
    companion object : CommonResponseTypeFactory<OptionalShortArrayResponse, ShortArray?> {
        val STREAM_CODEC =
            packetCodec(OptionalShortArrayResponse::write, ::OptionalShortArrayResponse)

        override fun create(value: ShortArray?): OptionalShortArrayResponse {
            return OptionalShortArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readShortArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeShortArray(value) }
    }

    operator fun component1() = value
}

fun OptionalShortArrayResponsePacket.respond(value: ShortCollection?) =
    respond(value?.toShortArray())