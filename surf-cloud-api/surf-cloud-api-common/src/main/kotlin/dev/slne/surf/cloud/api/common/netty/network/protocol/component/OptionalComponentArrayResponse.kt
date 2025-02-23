package dev.slne.surf.cloud.api.common.netty.network.protocol.component

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component

typealias OptionalComponentArrayResponsePacket = RespondingNettyPacket<OptionalComponentArrayResponse>

@SurfNettyPacket("optional_component_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalComponentArrayResponse(override val value: Array<Component>?) : ResponseNettyPacket(),
    CommonResponseType<Array<Component>?> {
    companion object :
        CommonResponseTypeFactory<OptionalComponentArrayResponse, Array<Component>?> {
        val STREAM_CODEC =
            packetCodec(OptionalComponentArrayResponse::write, ::OptionalComponentArrayResponse)

        override fun create(value: Array<Component>?): OptionalComponentArrayResponse {
            return OptionalComponentArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readArray { it.readComponent() } })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, v -> buf.writeArray(v) { buf, v -> buf.writeComponent(v) } }
    }
}

fun OptionalComponentArrayResponsePacket.respond(value: Collection<Component>?) =
    respond(value?.toTypedArray())