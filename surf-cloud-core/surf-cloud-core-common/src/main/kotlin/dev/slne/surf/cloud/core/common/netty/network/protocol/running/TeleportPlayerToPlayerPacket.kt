package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import java.util.*

@SurfNettyPacket(
    "cloud:bidirectional:teleport_player_to_player",
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class TeleportPlayerToPlayerPacket(
    val uuid: UUID,
    val target: UUID,
) : BooleanResponsePacket(), InternalNettyPacket<CommonRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            TeleportPlayerToPlayerPacket::uuid,
            ByteBufCodecs.UUID_CODEC,
            TeleportPlayerToPlayerPacket::target,
            ::TeleportPlayerToPlayerPacket
        )
    }

    override fun handle(listener: CommonRunningPacketListener) {
        listener.handleTeleportPlayerToPlayer(this)
    }
}