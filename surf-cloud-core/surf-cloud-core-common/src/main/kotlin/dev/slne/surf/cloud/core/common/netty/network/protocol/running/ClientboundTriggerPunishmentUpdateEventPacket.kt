package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment

@SurfNettyPacket(
    "clientbound:cloud:trigger_punishment_update_event",
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ClientboundTriggerPunishmentUpdateEventPacket(
    val updatedPunishment: AbstractPunishment,
    val operation: CloudPlayerPunishmentUpdatedEvent.Operation
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            AbstractPunishment.STREAM_CODEC,
            ClientboundTriggerPunishmentUpdateEventPacket::updatedPunishment,
            CloudPlayerPunishmentUpdatedEvent.Operation.STREAM_CODEC,
            ClientboundTriggerPunishmentUpdateEventPacket::operation,
            ::ClientboundTriggerPunishmentUpdateEventPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleTriggerPunishmentUpdateEvent(this)
    }
}