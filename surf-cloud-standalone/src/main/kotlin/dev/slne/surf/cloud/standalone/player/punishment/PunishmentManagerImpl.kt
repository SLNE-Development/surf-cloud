package dev.slne.surf.cloud.standalone.player.punishment

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.api.common.util.toObjectList
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.api.server.netty.packet.broadcast
import dev.slne.surf.cloud.core.common.coroutines.PunishmentDatabaseScope
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTriggerPunishmentCreatedEventPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTriggerPunishmentUpdateEventPacket
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.PunishmentCacheImpl
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.*
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.util.publish
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity.*
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration.Companion.minutes
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent.Operation as UpdateOperation

@Component
@Order(PrePlayerJoinTask.PUNISHMENT_MANAGER)
class PunishmentManagerImpl : PunishmentManager, PrePlayerJoinTask {

    private val log = logger()

    private val preJoinPunishmentCache = Caffeine.newBuilder()
        .expireAfterWrite(2.minutes)
        .build<UUID, PunishmentCacheImpl>()

    companion object {
        @Suppress("SpellCheckingInspection")
        private const val PUNISHMENT_ID_CHARS = "ABCDEFGHIJKLMNPQRTUVWXYZabcdefghijklmnpqrtuvwxyz"
        private const val PUNISHMENT_ID_LENGTH = 6
    }

    override suspend fun generatePunishmentId(): String {
        val id = buildString(PUNISHMENT_ID_LENGTH) {
            repeat(PUNISHMENT_ID_LENGTH) {
                append(PUNISHMENT_ID_CHARS[random.nextInt(PUNISHMENT_ID_CHARS.length)])
            }
        }

        val banCount = withTransactionAsync {
            BanPunishmentTable.selectAll()
                .where { BanPunishmentTable.punishmentId eq id }
                .count()
        }

        val kickCount = withTransactionAsync {
            KickPunishmentTable.selectAll()
                .where { KickPunishmentTable.punishmentId eq id }
                .count()
        }

        val muteCount = withTransactionAsync {
            MutePunishmentTable.selectAll()
                .where { MutePunishmentTable.punishmentId eq id }
                .count()
        }

        val warningCount = withTransactionAsync {
            WarnPunishmentTable.selectAll()
                .where { WarnPunishmentTable.punishmentId eq id }
                .count()
        }

        if (banCount.await() + kickCount.await() + muteCount.await() + warningCount.await() > 0) {
            return generatePunishmentId()
        }

        return id
    }

    override suspend fun createKick(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        initialNotes: List<String>
    ): PunishmentKickImpl = withTransaction {
        val punishmentId = generatePunishmentId()
        val punishment = KickPunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
        }

        createNotes(initialNotes, punishment) { p, note ->
            KickPunishmentNoteEntity.new {
                this.punishment = p
                this.note = note
            }
        }

        punishment.toApiObject()
    }.also { broadcastPunishmentCreation(it) }


    override suspend fun createWarn(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        initialNotes: List<String>
    ): PunishmentWarnImpl = withTransaction {
        val punishmentId = generatePunishmentId()
        val punishment = WarnPunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
        }

        createNotes(initialNotes, punishment) { p, note ->
            WarnPunishmentNoteEntity.new {
                this.punishment = p
                this.note = note
            }
        }

        punishment.toApiObject()
    }.also { broadcastPunishmentCreation(it) }


    override suspend fun createMute(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        initialNotes: List<String>
    ): PunishmentMuteImpl = withTransaction {
        val punishmentId = generatePunishmentId()
        val punishment = MutePunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
            this.expirationDate = expirationDate
            this.permanent = permanent
        }

        createNotes(initialNotes, punishment) { p, note ->
            MutePunishmentNoteEntity.new {
                this.punishment = p
                this.note = note
            }
        }

        punishment.toApiObject()
    }.also { broadcastPunishmentCreation(it) }

    override suspend fun createBan(
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        securityBan: Boolean,
        raw: Boolean,
        initialNotes: List<String>,
        initialIpAddresses: List<String>
    ): PunishmentBanImpl = withTransaction {
        val punishmentId = generatePunishmentId()
        val punishment = BanPunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
            this.expirationDate = expirationDate
            this.permanent = permanent
            this.securityBan = securityBan
            this.raw = raw
        }

        createNotes(initialNotes, punishment) { p, note ->
            BanPunishmentNoteEntity.new {
                this.punishment = p
                this.note = note
            }
        }

        for (ip in initialIpAddresses) {
            BanPunishmentIpAddressEntity.new {
                this.punishment = punishment
                this.ipAddress = ip
            }
        }

        punishment.toApiObject()
    }.also { broadcastPunishmentCreation(it) }

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

    private fun <T : LongEntity> createNotes(
        notes: List<String>,
        punishment: T,
        factory: (T, String) -> Unit
    ) {
        for (note in notes) {
            factory(punishment, note)
        }
    }

    override suspend fun attachIpAddressToBan(id: Long, rawIp: String): Boolean {
        val (exists, ban, ip) = withTransaction {
            val exists = BanPunishmentIpAddressTable
                .select(BanPunishmentIpAddressTable.id)
                .where {
                    (BanPunishmentIpAddressTable.punishment eq id) and (BanPunishmentIpAddressTable.ipAddress eq rawIp)
                }
                .limit(1)
                .any()

            if (exists) {
                return@withTransaction Triple(true, null, null)
            }

            val ban = BanPunishmentEntity.findById(id) ?: error("Ban with id $id not found")
            val note = BanPunishmentIpAddressEntity.new {
                this.punishment = ban
                this.ipAddress = rawIp
            }

            Triple(false, ban.toApiObject(), note.toApiObject())
        }

        if (exists) {
            return false
        }

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
        notEntityClass: LongEntityClass<NoteEntity>,
        note: String,
        toApi: (PunishmentEntity) -> Api,
    ): PunishmentNoteImpl {
        val (punishment, note) = withTransaction {
            val entity = entityClass.findById(id) ?: error("Punishment with id $id not found")
            val note = notEntityClass.new {
                this.note = note
                this.punishment = entity
            }

            toApi(entity) to note.toApiObject()
        }

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
        withTransaction {
            BanPunishmentIpAddressEntity.find { BanPunishmentIpAddressTable.punishment eq id }
                .map { it.toApiObject() }
        }

    private suspend fun <P : AbstractPunishmentEntity, E : AbstractPunishmentNoteEntity<P>> fetchNotesForPunishment(
        id: Long,
        noteEntityClass: LongEntityClass<E>
    ) = withTransaction {
        noteEntityClass.find { BanPunishmentNoteTable.punishment eq id }
            .map { it.toApiObject() }
    }

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
    ): List<PunishmentBanImpl> = withTransaction {
        val banIds = BanPunishmentIpAddressTable.select(BanPunishmentIpAddressTable.punishment)
            .where { BanPunishmentIpAddressTable.ipAddress eq ip }
            .map { it[BanPunishmentIpAddressTable.punishment] }
            .toSet()

        if (banIds.isEmpty()) {
            return@withTransaction emptyList()
        }

        val query = if (onlyActive) {
            BanPunishmentEntity.find { activePunishmentFilter(BanPunishmentTable) and (BanPunishmentTable.id inList banIds) }
        } else {
            BanPunishmentEntity.find { BanPunishmentTable.id inList banIds }
        }

        query.map { it.toApiObject() }
    }


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
    ): List<T> {
        return withTransaction {
            val query = if (onlyActive) {
                entityClass.find {
                    punishedUuidFilter(table, punishedUuid) and activePunishmentFilter(table)
                }
            } else {
                entityClass.find {
                    punishedUuidFilter(table, punishedUuid)
                }
            }

            query.map { toApiObject(it) }
        }
    }

    private suspend fun <E : AbstractPunishmentEntity, T> fetchPunishments(
        entityClass: AuditableLongEntityClass<E>,
        table: AbstractPunishmentTable,
        punishedUuid: UUID,
        toApiObject: (E) -> T
    ): List<T> {
        return withTransaction {
            entityClass.find { punishedUuidFilter(table, punishedUuid) }.map { toApiObject(it) }
        }
    }

    private fun <T : AbstractUnpunishableExpirablePunishmentTable> activePunishmentFilter(
        table: T
    ): Op<Boolean> {
        return (table.unpunished eq false) and
                ((table.permanent eq true) or
                        (table.expirationDate greater ZonedDateTime.now()))
    }

    private fun <T : AbstractPunishmentTable> punishedUuidFilter(
        table: T,
        punishedUuid: UUID
    ): Op<Boolean> {
        return (table.punishedUuid eq punishedUuid)
    }

    override suspend fun getCurrentLoginValidationPunishmentCache(playerUuid: UUID): PunishmentCacheImpl? {
        return preJoinPunishmentCache.getIfPresent(playerUuid)
    }

    override suspend fun preJoin(player: CommonOfflineCloudPlayerImpl): PrePlayerJoinTask.Result {
        val uuid = player.uuid
        preJoinPunishmentCache.put(uuid, fetchPunishmentCache(uuid))
        return PrePlayerJoinTask.Result.ALLOWED
    }

    private suspend fun fetchPunishmentCache(uuid: UUID): PunishmentCacheImpl = coroutineScope {
        val mutesDeferred = withTransactionAsync {
            fetchPunishments(MutePunishmentEntity, MutePunishmentTable, uuid) { it.toApiObject() }
        }
        val bansDeferred = withTransactionAsync {
            fetchPunishments(BanPunishmentEntity, BanPunishmentTable, uuid) { it.toApiObject() }
        }

        val kicksDeferred = withTransactionAsync {
            fetchPunishments(KickPunishmentEntity, KickPunishmentTable, uuid) { it.toApiObject() }
        }

        val warningsDeferred = withTransactionAsync {
            fetchPunishments(WarnPunishmentEntity, WarnPunishmentTable, uuid) { it.toApiObject() }
        }

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

    private suspend fun <T> withTransaction(statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(PunishmentDatabaseScope.context, statement = statement)

    private suspend fun <T> withTransactionAsync(statement: suspend Transaction.() -> T) =
        suspendedTransactionAsync(PunishmentDatabaseScope.context, statement = statement)
}