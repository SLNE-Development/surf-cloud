package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(DefaultIds.CLIENTBOUND_SEND_MESSAGE_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundSendMessagePacket : NettyPacket {
    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundSendMessagePacket::write, ::ClientboundSendMessagePacket)
    }

    val uuid: UUID
    val message: Component
    val permission: String?

    constructor(uuid: UUID, message: Component, permission: String? = null) {
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