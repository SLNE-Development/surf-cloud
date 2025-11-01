package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.BIDIRECTIONAL_TELEPORT_PLAYER_RESULT, PacketFlow.BIDIRECTIONAL)
class TeleportPlayerResultPacket : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            TeleportPlayerResultPacket::write,
            ::TeleportPlayerResultPacket
        )
    }

    val result: Boolean

    constructor(result: Boolean) {
        this.result = result
    }

    private constructor(buf: SurfByteBuf) {
        this.result = buf.readBoolean()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeBoolean(result)
    }
}