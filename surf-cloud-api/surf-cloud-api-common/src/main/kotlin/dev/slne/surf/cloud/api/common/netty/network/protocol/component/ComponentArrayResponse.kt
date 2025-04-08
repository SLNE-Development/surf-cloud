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

typealias ComponentArrayResponsePacket = RespondingNettyPacket<ComponentArrayResponse>

@SurfNettyPacket("component_array_response", PacketFlow.BIDIRECTIONAL)
class ComponentArrayResponse(override val value: Array<Component>) : ResponseNettyPacket(),
    CommonResponseType<Array<Component>> {
    companion object : CommonResponseTypeFactory<ComponentArrayResponse, Array<Component>> {
        val STREAM_CODEC = packetCodec(ComponentArrayResponse::write, ::ComponentArrayResponse)
        override fun create(value: Array<Component>): ComponentArrayResponse {
            return ComponentArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readArray { buf.readComponent() })

    private fun write(buf: SurfByteBuf) {
        buf.writeArray(value) { buf, value -> buf.writeComponent(value) }
    }
}

fun ComponentArrayResponsePacket.respond(value: Collection<Component>) =
    respond(value.toTypedArray())