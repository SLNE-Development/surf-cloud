package dev.slne.surf.cloud.standalone.player.punishment

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.api.server.netty.packet.broadcast
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundTriggerPunishmentCreatedEventPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundTriggerPunishmentUpdateEventPacket
import dev.slne.surf.cloud.core.common.player.PunishmentCacheImpl
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.*
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity.*
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.random
import dev.slne.surf.surfapi.core.api.util.toObjectList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntityClass
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration.Companion.minutes
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent.Operation as UpdateOperation

@Component
@Order(PrePlayerJoinTask.PUNISHMENT_MANAGER)
class PunishmentManagerImpl(private val service: PunishmentService) : PunishmentManager,
    PrePlayerJoinTask {

    private val log = logger()

    private val preJoinPunishmentCache = Caffeine.newBuilder()
        .expireAfterWrite(2.minutes)
        .build<UUID, PunishmentCacheImpl>()

    companion object {
        @Suppress("SpellCheckingInspection")
        private const val PUNISHMENT_ID_CHARS = "ABCDEFGHIJKLMNPQRTUVWXYZabcdefghijklmnpqrtuvwxyz"
        private const val PUNISHMENT_ID_LENGTH = 6
    }

    override suspend fun generatePunishmentId(): String = coroutineScope {
        val id = buildString(PUNISHMENT_ID_LENGTH) {
            repeat(PUNISHMENT_ID_LENGTH) {
                append(PUNISHMENT_ID_CHARS[random.nextInt(PUNISHMENT_ID_CHARS.length)])
            }
        }

        val banCount = async { service.countBansByPunishmentId(id) }
        val kickCount = async { service.countKicksByPunishmentId(id) }
        val muteCount = async { service.countMutesByPunishmentId(id) }
        val warningCount = async { service.countWarningsByPunishmentId(id) }

        if (banCount.await() + kickCount.await() + muteCount.await() + warningCount.await() > 0) {
            generatePunishmentId()
        } else {
            id
        }
    }

    override suspend fun createKick(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentKickImpl {
        val punishmentId = generatePunishmentId()
        val kick = service.createKickWithNotes(
            punishmentId,
            punishedUuid,
            issuerUuid,
            reason,
            initialNotes
        )

        broadcastPunishmentCreation(kick)
        return kick
    }


    override suspend fun createWarn(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentWarnImpl {
        val punishmentId = generatePunishmentId()
        val warning = service.createWarningWithNotes(
            punishmentId,
            punishedUuid,
            issuerUuid,
            reason,
            initialNotes
        )

        broadcastPunishmentCreation(warning)
        return warning
    }


    override suspend fun createMute(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        initialNotes: List<String>
    ): PunishmentMuteImpl {
        val punishmentId = generatePunishmentId()
        val punishment = service.createMuteWithNotes(
            punishmentId,
            punishedUuid,
            issuerUuid,
            reason,
            permanent,
            expirationDate,
            initialNotes
        )

        broadcastPunishmentCreation(punishment)
        return punishment
    }

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
    ): PunishmentBanImpl {
        val punishmentId = generatePunishmentId()
        val punishment = service.createBanWithNotesAndIpAddresses(
            punishmentId,
            punishedUuid,
            issuerUuid,
            reason,
            permanent,
            expirationDate,
            securityBan,
            raw,
            initialNotes,
            initialIpAddresses
        )

        broadcastPunishmentCreation(punishment)
        return punishment
    }

    override suspend fun broadcastBan(ban: PunishmentBanImpl) {
        broadcastPunishmentCreation(ban)
    }

    override suspend fun broadcastMute(mute: PunishmentMuteImpl) {
        broadcastPunishmentCreation(mute)
    }

    override suspend fun broadcastKick(kick: PunishmentKickImpl) {
        broadcastPunishmentCreation(kick)
    }

    override suspend fun broadcastWarn(warn: PunishmentWarnImpl) {
        broadcastPunishmentCreation(warn)
    }

    override suspend fun broadcastBanUpdate(ban: PunishmentBanImpl) {
        broadcastPunishmentUpdate(ban, UpdateOperation.ADMIN_PANEL)
    }

    override suspend fun broadcastMuteUpdate(mute: PunishmentMuteImpl) {
        broadcastPunishmentUpdate(mute, UpdateOperation.ADMIN_PANEL)
    }

    override suspend fun broadcastKickUpdate(kick: PunishmentKickImpl) {
        broadcastPunishmentUpdate(kick, UpdateOperation.ADMIN_PANEL)
    }

    override suspend fun broadcastWarnUpdate(warn: PunishmentWarnImpl) {
        broadcastPunishmentUpdate(warn, UpdateOperation.ADMIN_PANEL)
    }

    override suspend fun attachIpAddressToBan(id: Long, rawIp: String): Boolean {
        val (exists, ban, ip) = service.attachIpAddressToBan(id, rawIp)
        if (exists) return false

        broadcastPunishmentUpdate(ban!!, UpdateOperation.ATTACH_IP(ip!!))
        return true
    }

    override suspend fun attachNoteToBan(id: Long, note: String): PunishmentNoteImpl =
        attachNoteToPunishment(
            id,
            BanPunishmentEntity,
            BanPunishmentNoteEntity,
            note
        ) { it.toApiObject() }

    override suspend fun attachNoteToKick(id: Long, note: String): PunishmentNoteImpl =
        attachNoteToPunishment(
            id,
            KickPunishmentEntity,
            KickPunishmentNoteEntity,
            note
        ) { it.toApiObject() }

    override suspend fun attachNoteToMute(id: Long, note: String): PunishmentNoteImpl =
        attachNoteToPunishment(
            id,
            MutePunishmentEntity,
            MutePunishmentNoteEntity,
            note
        ) { it.toApiObject() }

    override suspend fun attachNoteToWarn(id: Long, note: String): PunishmentNoteImpl =
        attachNoteToPunishment(
            id,
            WarnPunishmentEntity,
            WarnPunishmentNoteEntity,
            note
        ) { it.toApiObject() }

    suspend fun <PunishmentEntity : AbstractPunishmentEntity, NoteEntity : AbstractPunishmentNoteEntity<PunishmentEntity>, Api : AbstractPunishment> attachNoteToPunishment(
        id: Long,
        entityClass: LongEntityClass<PunishmentEntity>,
        noteEntityClass: LongEntityClass<NoteEntity>,
        note: String,
        toApi: (PunishmentEntity) -> Api,
    ): PunishmentNoteImpl {
        val (punishment, note) = service.attachNoteToPunishment(
            id,
            entityClass,
            noteEntityClass,
            note,
            toApi
        )
        broadcastPunishmentUpdate(punishment, UpdateOperation.NOTE_ADDED(note))
        return note
    }

    override suspend fun fetchNotesForBan(id: Long): List<PunishmentNoteImpl> =
        fetchNotesForPunishment(id, BanPunishmentNoteEntity)

    override suspend fun fetchNotesForKick(id: Long): List<PunishmentNoteImpl> =
        fetchNotesForPunishment(id, KickPunishmentNoteEntity)

    override suspend fun fetchNotesForMute(id: Long): List<PunishmentNoteImpl> =
        fetchNotesForPunishment(id, MutePunishmentNoteEntity)

    override suspend fun fetchNotesForWarn(id: Long): List<PunishmentNoteImpl> =
        fetchNotesForPunishment(id, WarnPunishmentNoteEntity)

    override suspend fun fetchIpAddressesForBan(id: Long): List<PunishmentAttachedIpAddressImpl> =
        service.fetchIpAddressesForBan(id)

    private suspend fun <P : AbstractPunishmentEntity, E : AbstractPunishmentNoteEntity<P>> fetchNotesForPunishment(
        id: Long,
        noteEntityClass: LongEntityClass<E>
    ) = service.fetchNotesForPunishment(id, noteEntityClass)

    override suspend fun fetchMutes(
        punishedUuid: UUID,
        onlyActive: Boolean
    ): List<PunishmentMuteImpl> = fetchPunishments(
        MutePunishmentEntity,
        MutePunishmentTable,
        punishedUuid,
        onlyActive
    ) { it.toApiObject() }

    override suspend fun fetchBans(
        punishedUuid: UUID,
        onlyActive: Boolean
    ): List<PunishmentBanImpl> = fetchPunishments(
        BanPunishmentEntity,
        BanPunishmentTable,
        punishedUuid,
        onlyActive
    ) { it.toApiObject() }

    override suspend fun fetchIpBans(
        ip: String,
        onlyActive: Boolean
    ): List<PunishmentBanImpl> = service.fetchIpBans(ip, onlyActive)

    override suspend fun fetchWarnings(punishedUuid: UUID): List<PunishmentWarnImpl> =
        fetchPunishments(
            WarnPunishmentEntity,
            WarnPunishmentTable,
            punishedUuid
        ) { it.toApiObject() }

    override suspend fun fetchKicks(punishedUuid: UUID): List<PunishmentKickImpl> =
        fetchPunishments(
            KickPunishmentEntity,
            KickPunishmentTable,
            punishedUuid
        ) { it.toApiObject() }

    private suspend fun <E : AbstractUnpunishableExpirablePunishmentEntity, T> fetchPunishments(
        entityClass: AuditableLongEntityClass<E>,
        table: AbstractUnpunishableExpirablePunishmentTable,
        punishedUuid: UUID,
        onlyActive: Boolean,
        toApiObject: (E) -> T
    ): List<T> = service.fetchPunishments(entityClass, table, punishedUuid, onlyActive, toApiObject)

    private suspend fun <E : AbstractPunishmentEntity, T> fetchPunishments(
        entityClass: AuditableLongEntityClass<E>,
        table: AbstractPunishmentTable,
        punishedUuid: UUID,
        toApiObject: (E) -> T
    ): List<T> = service.fetchPunishments(entityClass, table, punishedUuid, toApiObject)

    override suspend fun getCurrentLoginValidationPunishmentCache(playerUuid: UUID): PunishmentCacheImpl? {
        return preJoinPunishmentCache.getIfPresent(playerUuid)
    }

    override suspend fun preJoin(player: OfflineCloudPlayer): PrePlayerJoinTask.Result {
        val uuid = player.uuid
        preJoinPunishmentCache.put(uuid, fetchPunishmentCache(uuid))
        return PrePlayerJoinTask.Result.ALLOWED
    }

    private suspend fun fetchPunishmentCache(uuid: UUID): PunishmentCacheImpl = coroutineScope {
        val mutesDeferred = async { fetchMutes(uuid, onlyActive = false) }
        val bansDeferred = async { fetchBans(uuid, onlyActive = false) }
        val kicksDeferred = async { fetchKicks(uuid) }
        val warningsDeferred = async { fetchWarnings(uuid) }

        PunishmentCacheImpl(
            mutes = mutesDeferred.await().sorted().toObjectList(),
            bans = bansDeferred.await().sorted().toObjectList(),
            kicks = kicksDeferred.await().toObjectList(),
            warnings = warningsDeferred.await().toObjectList()
        )
    }

    private fun <P : AbstractPunishment> broadcastPunishmentUpdate(
        updatedPunishment: P,
        operation: UpdateOperation
    ) {
        try {
            val event = CloudPlayerPunishmentUpdatedEvent(
                this,
                updatedPunishment.punishedPlayer(),
                updatedPunishment,
                operation
            )

            PunishmentHandlerScope.launch {
                event.post()
                ClientboundTriggerPunishmentUpdateEventPacket(
                    updatedPunishment,
                    operation
                ).broadcast()
            }
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to broadcast punishment update event for punishment $updatedPunishment")
        }
    }

    private fun <P : AbstractPunishment> broadcastPunishmentCreation(createdPunishment: P) {
        try {
            val event = CloudPlayerPunishEvent(
                this,
                createdPunishment.punishedPlayer(),
                createdPunishment,
            )

            PunishmentHandlerScope.launch {
                event.post()
                ClientboundTriggerPunishmentCreatedEventPacket(createdPunishment).broadcast()
            }
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to broadcast punishment create event for punishment $createdPunishment")
        }
    }
}