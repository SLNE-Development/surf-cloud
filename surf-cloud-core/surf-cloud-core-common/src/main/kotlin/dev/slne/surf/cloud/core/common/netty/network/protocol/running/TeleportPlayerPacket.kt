package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.api.common.util.IdRepresentable.Companion.apply
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import java.util.*

@SurfNettyPacket(
    DefaultIds.BIDIRECTIONAL_TELEPORT_PLAYER,
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class TeleportPlayerPacket(
    val uuid: UUID,
    val location: WorldLocation,
    val teleportCause: TeleportCause,
    val flags: EnumSet<TeleportFlag>
) : BooleanResponsePacket(), InternalNettyPacket<CommonRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            TeleportPlayerPacket::uuid,
            WorldLocation.STREAM_CODEC,
            TeleportPlayerPacket::location,
            TeleportCause.STREAM_CODEC,
            TeleportPlayerPacket::teleportCause,
            TeleportFlag.STREAM_CODEC.apply(ByteBufCodecs.idEnumSet()),
            TeleportPlayerPacket::flags,
            ::TeleportPlayerPacket
        )
    }

    override fun handle(listener: CommonRunningPacketListener) {
        listener.handleTeleportPlayer(this)
    }
}