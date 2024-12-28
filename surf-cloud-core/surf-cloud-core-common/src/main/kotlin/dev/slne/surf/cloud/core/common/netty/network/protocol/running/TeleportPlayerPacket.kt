package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import dev.slne.surf.cloud.api.common.util.position.FineLocation
import dev.slne.surf.cloud.api.common.util.position.FineTeleportCause
import dev.slne.surf.cloud.api.common.util.position.FineTeleportFlag
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
    val location: FineLocation
    val teleportCause: FineTeleportCause
    val flags: Array<out FineTeleportFlag>

    constructor(
        uuid: UUID,
        location: FineLocation,
        teleportCause: FineTeleportCause,
        vararg flags: FineTeleportFlag
    ) {
        this.uuid = uuid
        this.location = location
        this.teleportCause = teleportCause
        this.flags = flags
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.location = FineLocation.STREAM_CODEC.decode(buf)
        this.flags = buf.readArray { it.readEnum() }
        this.teleportCause = buf.readEnum()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        FineLocation.STREAM_CODEC.encode(buf, location)
        buf.writeArray(flags, SurfByteBuf::writeEnum)
        buf.writeEnum(teleportCause)
    }
}