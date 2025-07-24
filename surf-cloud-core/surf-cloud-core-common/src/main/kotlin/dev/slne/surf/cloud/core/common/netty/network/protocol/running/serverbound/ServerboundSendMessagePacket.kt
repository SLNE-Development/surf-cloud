package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_SEND_MESSAGE_PACKET, PacketFlow.SERVERBOUND)
class ServerboundSendMessagePacket : NettyPacket {
    companion object {
        val STREAM_CODEC =
            packetCodec(ServerboundSendMessagePacket::write, ::ServerboundSendMessagePacket)
    }

    val uuid: UUID
    val message: Component
    val permission: String?

    constructor(uuid: UUID, message: Component) {
        this.uuid = uuid
        this.message = message
        this.permission = null
    }

    constructor(uuid: UUID, message: Component, permission: String) {
        this.uuid = uuid
        this.message = message
        this.permission = permission
    }

    private constructor(buffer: SurfByteBuf) {
        uuid = buffer.readUuid()
        message = buffer.readComponent()
        permission = buffer.readNullableString()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUuid(uuid)
        buffer.writeComponent(message)
        buffer.writeNullable(permission)
    }
}