package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
@SurfNettyPacket("cloud:serverbound:create_kick", PacketFlow.SERVERBOUND)
class ServerboundCreateKickPacket(
    val punishedUuid: @Contextual UUID,
    val issuerUuid: @Contextual UUID?,
    val reason: String?,
    val initialNotes: List<String>,
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:create_warn", PacketFlow.SERVERBOUND)
class ServerboundCreateWarnPacket(
    val punishedUuid: @Contextual UUID,
    val issuerUuid: @Contextual UUID?,
    val reason: String?,
    val initialNotes: List<String>,
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:create_mute", PacketFlow.SERVERBOUND)
class ServerboundCreateMutePacket(
    val punishedUuid: @Contextual UUID,
    val issuerUuid: @Contextual UUID?,
    val reason: String?,
    val permanent: Boolean,
    val expirationDate: @Contextual ZonedDateTime?,
    val initialNotes: List<String>,
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:serverbound:create_ban", PacketFlow.SERVERBOUND)
class ServerboundCreateBanPacket(
    val punishedUuid: @Contextual UUID,
    val issuerUuid: @Contextual UUID?,
    val reason: String?,
    val permanent: Boolean,
    val expirationDate: @Contextual ZonedDateTime?,
    val securityBan: Boolean,
    val raw: Boolean,
    val initialNotes: List<String>,
    val initialIpAddresses: List<String>,
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:clientbound:created_punishment_response", PacketFlow.CLIENTBOUND)
class ClientboundCreatedPunishmentResponsePacket(val punishment: AbstractPunishment) :
    ResponseNettyPacket()