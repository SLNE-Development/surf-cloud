package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment

@SurfNettyPacket(
    "clientbound:cloud:trigger_punishment_created_event",
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ClientboundTriggerPunishmentCreatedEventPacket(val createdPunishment: AbstractPunishment) :
    NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            AbstractPunishment.STREAM_CODEC,
            ClientboundTriggerPunishmentCreatedEventPacket::createdPunishment,
            ::ClientboundTriggerPunishmentCreatedEventPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleTriggerPunishmentCreatedEvent(this)
    }
}