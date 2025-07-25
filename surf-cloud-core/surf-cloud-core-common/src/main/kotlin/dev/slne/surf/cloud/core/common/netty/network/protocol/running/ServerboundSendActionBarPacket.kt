package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.UUID

@SurfNettyPacket(DefaultIds.SERVERBOUND_SEND_ACTION_BAR_PACKET, PacketFlow.SERVERBOUND)
class ServerboundSendActionBarPacket: NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(ServerboundSendActionBarPacket::write, ::ServerboundSendActionBarPacket)
    }

    val uuid: UUID
    val message: Component

    constructor(uuid: UUID, message: Component) {
        this.uuid = uuid
        this.message = message
    }

    private constructor(buffer: SurfByteBuf) {
        uuid = buffer.readUuid()
        message = buffer.readComponent()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUuid(uuid)
        buffer.writeComponent(message)
    }
}