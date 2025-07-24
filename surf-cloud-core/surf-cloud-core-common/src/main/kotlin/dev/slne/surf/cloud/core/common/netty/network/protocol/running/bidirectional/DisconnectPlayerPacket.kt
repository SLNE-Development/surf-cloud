package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(DefaultIds.BIDIRECTIONAL_DISCONNECT_PLAYER, PacketFlow.BIDIRECTIONAL)
class DisconnectPlayerPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            DisconnectPlayerPacket::write,
            ::DisconnectPlayerPacket
        )
    }

    val uuid: UUID
    val reason: Component

    constructor(uuid: UUID, reason: Component) {
        this.uuid = uuid
        this.reason = reason
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.reason = buf.readComponent()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeComponent(reason)
    }
}