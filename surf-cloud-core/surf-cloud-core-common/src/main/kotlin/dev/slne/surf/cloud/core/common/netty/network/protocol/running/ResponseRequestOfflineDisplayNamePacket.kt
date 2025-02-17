package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component

@SurfNettyPacket(DefaultIds.RESPONSE_REQUEST_OFFLINE_DISPLAY_NAME_PACKET, PacketFlow.BIDIRECTIONAL)
data class ResponseRequestOfflineDisplayNamePacket(val displayName: Component?) :
    ResponseNettyPacket() {
    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readComponent() })

    companion object {
        val STREAM_CODEC = packetCodec(
            ResponseRequestOfflineDisplayNamePacket::write,
            ::ResponseRequestOfflineDisplayNamePacket
        )
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(displayName) { buf, value -> buf.writeComponent(value) }
    }
}