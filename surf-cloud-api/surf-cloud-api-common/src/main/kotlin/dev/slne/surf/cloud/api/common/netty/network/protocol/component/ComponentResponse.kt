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

typealias ComponentResponsePacket = RespondingNettyPacket<ComponentResponse>

@SurfNettyPacket("component_response", PacketFlow.BIDIRECTIONAL)
class ComponentResponse(override val value: Component) : ResponseNettyPacket(),
    CommonResponseType<Component> {
    companion object : CommonResponseTypeFactory<ComponentResponse, Component> {
        val STREAM_CODEC = packetCodec(ComponentResponse::write, ::ComponentResponse)
        override fun create(value: Component): ComponentResponse {
            return ComponentResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readComponent())

    private fun write(buf: SurfByteBuf) {
        buf.writeComponent(value)
    }

    operator fun component1() = value
}