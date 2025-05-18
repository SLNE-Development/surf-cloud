package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.awaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.player.PunishmentCacheImpl
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class PunishmentManagerImpl : PunishmentManager {
    override suspend fun generatePunishmentId(): String =
        ServerboundGeneratePunishmentIdPacket().awaitOrThrow()

    override suspend fun createKick(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentKickImpl = ServerboundCreateKickPacket(
        punishedUuid,
        issuerUuid,
        reason,
        initialNotes
    ).fireAndAwaitOrThrow().punishment as PunishmentKickImpl

    override suspend fun createWarn(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentWarnImpl = ServerboundCreateWarnPacket(
        punishedUuid,
        issuerUuid,
        reason,
        initialNotes
    ).fireAndAwaitOrThrow().punishment as PunishmentWarnImpl

    override suspend fun createMute(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        initialNotes: List<String>
    ): PunishmentMuteImpl = ServerboundCreateMutePacket(
        punishedUuid,
        issuerUuid,
        reason,
        permanent,
        expirationDate,
        initialNotes
    ).fireAndAwaitOrThrow().punishment as PunishmentMuteImpl

    override suspend fun createBan(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        securityBan: Boolean,
        raw: Boolean,
        initialNotes: List<String>,
        initialIpAddresses: List<String>
    ): PunishmentBanImpl = ServerboundCreateBanPacket(
        punishedUuid,
        issuerUuid,
        reason,
        permanent,
        expirationDate,
        securityBan,
        raw,
        initialNotes,
        initialIpAddresses
    ).fireAndAwaitOrThrow().punishment as PunishmentBanImpl

    override suspend fun broadcastBan(ban: PunishmentBanImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastMute(mute: PunishmentMuteImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastKick(kick: PunishmentKickImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastWarn(warn: PunishmentWarnImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastBanUpdate(ban: PunishmentBanImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastMuteUpdate(mute: PunishmentMuteImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastKickUpdate(kick: PunishmentKickImpl) {
        error("Can only be used on server")
    }

    override suspend fun broadcastWarnUpdate(warn: PunishmentWarnImpl) {
        error("Can only be used on server")
    }

    override suspend fun attachIpAddressToBan(id: Long, rawIp: String): Boolean {
        return ServerboundAttachIpAddressToBanPacket(id, rawIp).awaitOrThrow()
    }

    override suspend fun attachNoteToBan(
        id: Long,
        note: String
    ): PunishmentNoteImpl =
        ServerboundAttachNoteToPunishmentPacket(
            id,
            note,
            PunishmentType.BAN
        ).fireAndAwaitOrThrow().note


    override suspend fun attachNoteToMute(
        id: Long,
        note: String
    ): PunishmentNoteImpl =
        ServerboundAttachNoteToPunishmentPacket(
            id,
            note,
            PunishmentType.MUTE
        ).fireAndAwaitOrThrow().note

    override suspend fun attachNoteToKick(
        id: Long,
        note: String
    ): PunishmentNoteImpl =
        ServerboundAttachNoteToPunishmentPacket(
            id,
            note,
            PunishmentType.KICK
        ).fireAndAwaitOrThrow().note

    override suspend fun attachNoteToWarn(
        id: Long,
        note: String
    ): PunishmentNoteImpl =
        ServerboundAttachNoteToPunishmentPacket(
            id,
            note,
            PunishmentType.WARN
        ).fireAndAwaitOrThrow().note

    override suspend fun fetchNotesForBan(id: Long): List<PunishmentNoteImpl> =
        ServerboundFetchNotesFromPunishmentPacket(
            id,
            PunishmentType.BAN
        ).fireAndAwaitOrThrow().notes

    override suspend fun fetchNotesForMute(id: Long): List<PunishmentNoteImpl> =
        ServerboundFetchNotesFromPunishmentPacket(
            id,
            PunishmentType.MUTE
        ).fireAndAwaitOrThrow().notes

    override suspend fun fetchNotesForKick(id: Long): List<PunishmentNoteImpl> =
        ServerboundFetchNotesFromPunishmentPacket(
            id,
            PunishmentType.KICK
        ).fireAndAwaitOrThrow().notes

    override suspend fun fetchNotesForWarn(id: Long): List<PunishmentNoteImpl> =
        ServerboundFetchNotesFromPunishmentPacket(
            id,
            PunishmentType.WARN
        ).fireAndAwaitOrThrow().notes

    override suspend fun fetchIpAddressesForBan(id: Long): List<PunishmentAttachedIpAddressImpl> {
        return ServerboundFetchIpAddressesForBanPacket(id).fireAndAwaitOrThrow().ipAddresses
    }

    override suspend fun fetchMutes(
        punishedUuid: UUID,
        onlyActive: Boolean
    ): List<PunishmentMuteImpl> =
        ServerboundFetchMutesPacket(
            punishedUuid,
            onlyActive
        ).fireAndAwaitOrThrow().punishments as List<PunishmentMuteImpl>

    override suspend fun fetchBans(
        punishedUuid: UUID,
        onlyActive: Boolean
    ): List<PunishmentBanImpl> =
        ServerboundFetchBansPacket(
            punishedUuid,
            onlyActive
        ).fireAndAwaitOrThrow().punishments as List<PunishmentBanImpl>

    override suspend fun fetchIpBans(
        ip: String,
        onlyActive: Boolean
    ): List<PunishmentBanImpl> = ServerboundFetchIpBansPacket(
        onlyActive,
        ip
    ).fireAndAwaitOrThrow().punishments as List<PunishmentBanImpl>


    override suspend fun fetchWarnings(punishedUuid: UUID): List<PunishmentWarnImpl> =
        ServerboundFetchWarnsPacket(punishedUuid).fireAndAwaitOrThrow().punishments as List<PunishmentWarnImpl>

    override suspend fun fetchKicks(punishedUuid: UUID): List<PunishmentKickImpl> =
        ServerboundFetchKicksPacket(punishedUuid).fireAndAwaitOrThrow().punishments as List<PunishmentKickImpl>

    override suspend fun getCurrentLoginValidationPunishmentCache(playerUuid: UUID): PunishmentCacheImpl? =
        ServerboundGetCurrentLoginValidationPunishmentCachePacket(playerUuid).fireAndAwaitOrThrow().cache
}