package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.UUID

@SurfNettyPacket(DefaultIds.RESPONSE_DISPLAY_NAME_PACKET_REQUEST_PACKET, PacketFlow.BIDIRECTIONAL)
class ResponseDisplayNamePacketRequestPacket: ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ResponseDisplayNamePacketRequestPacket::write, ::ResponseDisplayNamePacketRequestPacket)
    }

    val uuid: UUID
    val displayName: Component

    constructor(uuid: UUID, displayName: Component) {
        this.uuid = uuid
        this.displayName = displayName
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.displayName = buf.readComponent()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeComponent(displayName)
    }
}