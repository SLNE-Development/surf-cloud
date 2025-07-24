package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_punishments_mutes", PacketFlow.SERVERBOUND)
data class ServerboundFetchMutesPacket(
    val punishedUuid: @Contextual UUID,
    val onlyActive: Boolean
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_punishments_bans", PacketFlow.SERVERBOUND)
data class ServerboundFetchBansPacket(
    val punishedUuid: @Contextual UUID,
    val onlyActive: Boolean,
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_ip_bans", PacketFlow.SERVERBOUND)
data class ServerboundFetchIpBansPacket(
    val onlyActive: Boolean,
    val ip: String,
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_punishments_kicks", PacketFlow.SERVERBOUND)
data class ServerboundFetchKicksPacket(val punishedUuid: @Contextual UUID) :
    RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_punishments_warns", PacketFlow.SERVERBOUND)
data class ServerboundFetchWarnsPacket(val punishedUuid: @Contextual UUID) :
    RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>()


@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_punishments_response", PacketFlow.CLIENTBOUND)
class ClientboundFetchedPunishmentsResponsePacket(val punishments: List<AbstractPunishment>) :
    ResponseNettyPacket()