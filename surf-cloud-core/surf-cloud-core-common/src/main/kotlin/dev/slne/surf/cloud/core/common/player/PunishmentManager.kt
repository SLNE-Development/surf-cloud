package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
interface PunishmentManager {
    suspend fun generatePunishmentId(): String

    suspend fun createKick(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        initialNotes: List<String>
    ): PunishmentKickImpl

    suspend fun createWarn(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        initialNotes: List<String>
    ): PunishmentWarnImpl

    suspend fun createMute(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        initialNotes: List<String>
    ): PunishmentMuteImpl

    suspend fun createBan(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        securityBan: Boolean,
        raw: Boolean,
        initialNotes: List<String>,
        initialIpAddresses: List<String>
    ): PunishmentBanImpl

    suspend fun broadcastBan(ban: PunishmentBanImpl)
    suspend fun broadcastMute(mute: PunishmentMuteImpl)
    suspend fun broadcastKick(kick: PunishmentKickImpl)
    suspend fun broadcastWarn(warn: PunishmentWarnImpl)
    suspend fun broadcastBanUpdate(ban: PunishmentBanImpl)
    suspend fun broadcastMuteUpdate(mute: PunishmentMuteImpl)
    suspend fun broadcastKickUpdate(kick: PunishmentKickImpl)
    suspend fun broadcastWarnUpdate(warn: PunishmentWarnImpl)

    suspend fun attachIpAddressToBan(id: Long, rawIp: String): Boolean

    suspend fun attachNoteToBan(id: Long, note: String): PunishmentNoteImpl
    suspend fun attachNoteToMute(id: Long, note: String): PunishmentNoteImpl
    suspend fun attachNoteToKick(id: Long, note: String): PunishmentNoteImpl
    suspend fun attachNoteToWarn(id: Long, note: String): PunishmentNoteImpl

    suspend fun fetchNotesForBan(id: Long): List<PunishmentNoteImpl>
    suspend fun fetchNotesForMute(id: Long): List<PunishmentNoteImpl>
    suspend fun fetchNotesForKick(id: Long): List<PunishmentNoteImpl>
    suspend fun fetchNotesForWarn(id: Long): List<PunishmentNoteImpl>

    suspend fun fetchIpAddressesForBan(id: Long): List<PunishmentAttachedIpAddressImpl>

    suspend fun fetchMutes(punishedUuid: UUID, onlyActive: Boolean): List<PunishmentMuteImpl>
    suspend fun fetchBans(punishedUuid: UUID, onlyActive: Boolean): List<PunishmentBanImpl>
    suspend fun fetchIpBans(ip: String, onlyActive: Boolean): List<PunishmentBanImpl>
    suspend fun fetchWarnings(punishedUuid: UUID): List<PunishmentWarnImpl>

    suspend fun fetchKicks(punishedUuid: UUID): List<PunishmentKickImpl>
    suspend fun getCurrentLoginValidationPunishmentCache(playerUuid: UUID): PunishmentCacheImpl?
}