package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import java.util.*

@SurfNettyPacket(DefaultIds.BIDIRECTIONAL_TELEPORT_PLAYER, PacketFlow.BIDIRECTIONAL)
class TeleportPlayerPacket : RespondingNettyPacket<TeleportPlayerResultPacket> {

    companion object {
        val STREAM_CODEC = packetCodec(
            TeleportPlayerPacket::write,
            ::TeleportPlayerPacket
        )
    }

    val uuid: UUID
    val location: TeleportLocation
    val teleportCause: TeleportCause
    val flags: Array<out TeleportFlag>

    constructor(
        uuid: UUID,
        location: TeleportLocation,
        teleportCause: TeleportCause,
        vararg flags: TeleportFlag
    ) {
        this.uuid = uuid
        this.location = location
        this.teleportCause = teleportCause
        this.flags = flags
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.location = TeleportLocation.STREAM_CODEC.decode(buf)
        this.flags = buf.readArray { it.readEnum() }
        this.teleportCause = buf.readEnum()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        TeleportLocation.STREAM_CODEC.encode(buf, location)
        buf.writeArray(flags, SurfByteBuf::writeEnum)
        buf.writeEnum(teleportCause)
    }
}