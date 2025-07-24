package dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("clientbound:cloud:trigger_punishment_created_event", PacketFlow.CLIENTBOUND)
data class ClientboundTriggerPunishmentCreatedEventPacket(val createdPunishment: AbstractPunishment) :
    NettyPacket()