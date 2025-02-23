package dev.slne.surf.cloud.api.common.netty.network.protocol.component

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component

typealias OptionalComponentResponsePacket = RespondingNettyPacket<OptionalComponentResponse>

@SurfNettyPacket("optional_component_response", PacketFlow.BIDIRECTIONAL)
class OptionalComponentResponse(override val value: Component?) : ResponseNettyPacket(),
    CommonResponseType<Component?> {
    companion object : CommonResponseTypeFactory<OptionalComponentResponse, Component?> {
        val STREAM_CODEC =
            packetCodec(OptionalComponentResponse::write, ::OptionalComponentResponse)

        override fun create(value: Component?): OptionalComponentResponse {
            return OptionalComponentResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readComponent() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeComponent(value) }
    }

    operator fun component1() = value
}