package dev.slne.surf.cloud.standalone.player.punishment

import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.cloud.core.common.player.punishment.type.*
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity.*
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*
import dev.slne.surf.surfapi.core.api.util.logger
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.springframework.stereotype.Service
import java.io.Serial
import java.time.ZonedDateTime
import java.util.*

/**
 * Exception thrown when a punishment or related entity is not found.
 */
class PunishmentNotFoundException(entity: String, id: Long) :
    NoSuchElementException("$entity punishment with id $id not found") {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -8983773040936961978L
    }
}

/**
 * Service for managing player punishments, including creation, querying, and note/IP operations.
 */
@Service
@CoroutineTransactional
class PunishmentService {
    private val log = logger()

    // region -- Count operations --

    /**
     * Counts the number of ban punishments with the given punishment ID.
     *
     * @param punishmentId unique identifier of the punishment
     * @return count of matching ban records
     */
    suspend fun countBansByPunishmentId(punishmentId: String): Long {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atFine().log("Counting bans for punishmentId=%s", punishmentId)
        return BanPunishmentTable.selectAll()
            .where { BanPunishmentTable.punishmentId eq punishmentId }
            .count()
    }

    /**
     * Counts the number of kick punishments with the given punishment ID.
     *
     * @param punishmentId unique identifier of the punishment
     * @return count of matching kick records
     */
    suspend fun countKicksByPunishmentId(punishmentId: String): Long {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atFine().log("Counting kicks for punishmentId=%s", punishmentId)
        return KickPunishmentTable.selectAll()
            .where { KickPunishmentTable.punishmentId eq punishmentId }
            .count()
    }

    /**
     * Counts the number of mute punishments with the given punishment ID.
     *
     * @param punishmentId unique identifier of the punishment
     * @return count of matching mute records
     */
    suspend fun countMutesByPunishmentId(punishmentId: String): Long {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atFine().log("Counting mutes for punishmentId=%s", punishmentId)
        return MutePunishmentTable.selectAll()
            .where { MutePunishmentTable.punishmentId eq punishmentId }
            .count()
    }

    /**
     * Counts the number of warning punishments with the given punishment ID.
     *
     * @param punishmentId unique identifier of the punishment
     * @return count of matching warning records
     */
    suspend fun countWarningsByPunishmentId(punishmentId: String): Long {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atFine().log("Counting warnings for punishmentId=%s", punishmentId)
        return WarnPunishmentTable.selectAll()
            .where { WarnPunishmentTable.punishmentId eq punishmentId }
            .count()
    }

    // endregion
    // region -- Creation with initial notes and IPs --

    /**
     * Creates a kick punishment with associated notes.
     *
     * @param punishmentId unique identifier for the punishment
     * @param punishedUuid UUID of the punished player
     * @param issuerUuid UUID of the issuer (nullable)
     * @param reason reason for the punishment (nullable)
     * @param initialNotes list of initial note texts
     * @return API representation of the created kick punishment
     */
    suspend fun createKickWithNotes(
        punishmentId: String,
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentKickImpl {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atInfo().log(
            "Creating Kick punishment id=%s punishedUuid=%s issuerUuid=%s",
            punishmentId,
            punishedUuid,
            issuerUuid
        )

        val punishment = KickPunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
        }

        if (initialNotes.isNotEmpty()) {
            KickPunishmentNoteTable.batchInsert(initialNotes) { noteText ->
                this[KickPunishmentNoteTable.punishment] = punishment.id
                this[KickPunishmentNoteTable.note] = noteText
            }
        }

        return punishment.toApiObject()
    }


    /**
     * Creates a warning punishment with associated notes.
     *
     * @param punishmentId unique identifier for the punishment
     * @param punishedUuid UUID of the punished player
     * @param issuerUuid UUID of the issuer (nullable)
     * @param reason reason for the punishment (nullable)
     * @param initialNotes list of initial note texts
     * @return API representation of the created warning punishment
     */
    suspend fun createWarningWithNotes(
        punishmentId: String,
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        initialNotes: List<String>
    ): PunishmentWarnImpl {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atInfo().log(
            "Creating Warn punishment id=%s punishedUuid=%s issuerUuid=%s",
            punishmentId,
            punishedUuid,
            issuerUuid
        )

        val punishment = WarnPunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
        }

        if (initialNotes.isNotEmpty()) {
            WarnPunishmentNoteTable.batchInsert(initialNotes) { noteText ->
                this[WarnPunishmentNoteTable.punishment] = punishment.id
                this[WarnPunishmentNoteTable.note] = noteText
            }
        }

        return punishment.toApiObject()
    }

    /**
     * Creates a mute punishment with associated notes.
     *
     * @param punishmentId unique identifier for the punishment
     * @param punishedUuid UUID of the punished player
     * @param issuerUuid UUID of the issuer (nullable)
     * @param reason reason for the punishment (nullable)
     * @param permanent indicates if the punishment is permanent
     * @param expirationDate date when the punishment expires (nullable)
     * @param initialNotes list of initial note texts
     * @return API representation of the created mute punishment
     */
    suspend fun createMuteWithNotes(
        punishmentId: String,
        punishedUuid: UUID,
        issuerUuid: UUID?,
        reason: String?,
        permanent: Boolean,
        expirationDate: ZonedDateTime?,
        initialNotes: List<String>
    ): PunishmentMuteImpl {
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atInfo().log(
            "Creating Mute punishment id=%s punishedUuid=%s issuerUuid=%s permanent=%s",
            punishmentId,
            punishedUuid,
            issuerUuid,
            permanent
        )

        val punishment = MutePunishmentEntity.new {
            this.punishmentId = punishmentId
            this.punishedUuid = punishedUuid
            this.issuerUuid = issuerUuid
            this.reason = reason
            this.expirationDate = expirationDate
            this.permanent = permanent
        }

        if (initialNotes.isNotEmpty()) {
            MutePunishmentNoteTable.batchInsert(initialNotes) { noteText ->
                this[MutePunishmentNoteTable.punishment] = punishment.id
                this[MutePunishmentNoteTable.note] = noteText
            }
        }

        return punishment.toApiObject()
    }

    /**
     * Creates a ban punishment with associated notes and IP addresses.
     *
     * @param punishmentId unique identifier for the punishment
     * @param punishedUuid UUID of the punished player
     * @param issuerUuid UUID of the issuer (nullable)
     * @param reason reason for the punishment (nullable)
     * @param permanent indicates if the punishment is permanent
     * @param expirationDate date when the punishment expires (nullable)
     * @param securityBan flag for security ban
     * @param raw raw flag
     * @param initialNotes list of initial note texts
     * @param initialIpAddresses list of IP addresses to attach
     * @return API representation of the created ban punishment
     */
    suspend fun createBanWithNotesAndIpAddresses(
        punishmentId: String,
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
        require(punishmentId.isNotBlank()) { "punishmentId must not be blank" }
        log.atInfo().log(
            "Creating Ban punishment id=%s punishedUuid=%s issuerUuid=%s permanent=%s securityBan=%s raw=%s",
            punishmentId,
            punishedUuid,
            issuerUuid,
            permanent,
            securityBan,
            raw
        )

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

        if (initialNotes.isNotEmpty()) {
            BanPunishmentNoteTable.batchInsert(initialNotes) { noteText ->
                this[BanPunishmentNoteTable.punishment] = punishment.id
                this[BanPunishmentNoteTable.note] = noteText
            }
        }
        if (initialIpAddresses.isNotEmpty()) {
            BanPunishmentIpAddressTable.batchInsert(initialIpAddresses) { ipText ->
                this[BanPunishmentIpAddressTable.punishment] = punishment.id
                this[BanPunishmentIpAddressTable.ipAddress] = ipText
            }
        }

        return punishment.toApiObject()
    }

    // endregion
    // region -- Attachment operations --

    /**
     * Attaches an IP address to an existing ban if not already present.
     *
     * @param id database ID of the ban punishment
     * @param rawIp IP address string to attach
     * @return Triple of (alreadyExists, banApiObject?, attachedIpApiObject?)
     */
    suspend fun attachIpAddressToBan(
        id: Long,
        rawIp: String
    ): Triple<Boolean, PunishmentBanImpl?, PunishmentAttachedIpAddressImpl?> {
        require(rawIp.isNotBlank()) { "IP address must not be blank" }
        log.atFine().log("Attaching IP %s to ban id=%s", rawIp, id)

        val exists = BanPunishmentIpAddressTable
            .select(BanPunishmentIpAddressTable.id)
            .where {
                (BanPunishmentIpAddressTable.punishment eq id) and (BanPunishmentIpAddressTable.ipAddress eq rawIp)
            }
            .limit(1)
            .any()

        if (exists) {
            log.atFine().log("IP %s already attached to ban id=%s", rawIp, id)
            return Triple(true, null, null)
        }

        val ban = BanPunishmentEntity.findById(id) ?: throw PunishmentNotFoundException("Ban", id)
        val ip = BanPunishmentIpAddressEntity.new {
            this.punishment = ban
            this.ipAddress = rawIp
        }
        log.atInfo().log("Attached IP %s to ban id=%s, entryId=%s", rawIp, id, ip.id.value)

        return Triple(false, ban.toApiObject(), ip.toApiObject())
    }

    /**
     * Attaches a note to a punishment entity.
     *
     * @param id database ID of the punishment
     * @param entityClass DAO class of the punishment
     * @param noteEntityClass DAO class of the note entity
     * @param note text of the note to attach
     * @param toApi function mapping entity to API object
     * @return Pair of (punishmentApiObject, createdNoteApiObject)
     */
    suspend fun <PunishmentEntity : AbstractPunishmentEntity, NoteEntity : AbstractPunishmentNoteEntity<PunishmentEntity>, Api : AbstractPunishment> attachNoteToPunishment(
        id: Long,
        entityClass: LongEntityClass<PunishmentEntity>,
        noteEntityClass: LongEntityClass<NoteEntity>,
        note: String,
        toApi: (PunishmentEntity) -> Api,
    ): Pair<Api, PunishmentNoteImpl> {
        require(note.isNotBlank()) { "Note must not be blank" }
        log.atFine().log("Attaching note to punishment id=%s note=%s", id, note)

        val entity = entityClass.findById(id) ?: throw PunishmentNotFoundException("Punishment", id)
        val note = noteEntityClass.new {
            this.note = note
            this.punishment = entity
        }
        log.atInfo().log("Attached note id=%s to punishment id=%s", note.id.value, id)

        return toApi(entity) to note.toApiObject()
    }

    // endregion
    // region -- Fetch operations --

    /**
     * Retrieves all IP addresses attached to a ban.
     *
     * @param id database ID of the ban
     * @return list of attached IP address API objects
     */
    suspend fun fetchIpAddressesForBan(id: Long): List<PunishmentAttachedIpAddressImpl> {
        log.atFine().log("Fetching IP addresses for ban id=%s", id)
        return BanPunishmentIpAddressEntity.find { BanPunishmentIpAddressTable.punishment eq id }
            .map { it.toApiObject() }
    }

    /**
     * Retrieves all notes for a given punishment.
     *
     * @param id database ID of the punishment
     * @param noteEntityClass DAO class of the note entity
     * @return list of note API objects
     */
    suspend fun <P : AbstractPunishmentEntity, E : AbstractPunishmentNoteEntity<P>> fetchNotesForPunishment(
        id: Long,
        noteEntityClass: LongEntityClass<E>
    ): List<PunishmentNoteImpl> {
        log.atFine().log("Fetching notes for punishment id=%s", id)
        return noteEntityClass.find { BanPunishmentNoteTable.punishment eq id }
            .map { it.toApiObject() }
    }

    /**
     * Finds ban punishments by IP address, optionally filtering active bans.
     *
     * @param ip IP address string to search
     * @param onlyActive whether to include only active bans
     * @return list of matching ban punishment API objects
     */
    suspend fun fetchIpBans(
        ip: String,
        onlyActive: Boolean
    ): List<PunishmentBanImpl> {
        require(ip.isNotBlank()) { "IP address must not be blank" }
        log.atFine().log("Fetching ip bans for ip=%s onlyActive=%s", ip, onlyActive)
        val banIds = BanPunishmentIpAddressTable.select(BanPunishmentIpAddressTable.punishment)
            .where { BanPunishmentIpAddressTable.ipAddress eq ip }
            .map { it[BanPunishmentIpAddressTable.punishment] }
            .toSet()

        if (banIds.isEmpty()) return emptyList()

        val now = ZonedDateTime.now()

        val query = if (onlyActive) {
            BanPunishmentEntity.find {
                activePunishmentFilter(
                    BanPunishmentTable,
                    now
                ) and (BanPunishmentTable.id inList banIds)
            }
        } else {
            BanPunishmentEntity.find { BanPunishmentTable.id inList banIds }
        }
        log.atFine().log("Found %s bans for ip=%s", query.count(), ip)

        return query.map { it.toApiObject() }
    }

    /**
     * Fetches punishments for a given player UUID, with optional filtering for only active expirable punishments.
     *
     * @param entityClass DAO class of the punishment entity
     * @param table corresponding table for filtering
     * @param punishedUuid UUID of the punished player
     * @param onlyActive whether to include only active punishments
     * @param toApiObject mapping from entity to API object
     * @return list of punishment API objects
     */
    suspend fun <E : AbstractUnpunishableExpirablePunishmentEntity, T> fetchPunishments(
        entityClass: AuditableLongEntityClass<E>,
        table: AbstractUnpunishableExpirablePunishmentTable,
        punishedUuid: UUID,
        onlyActive: Boolean,
        toApiObject: (E) -> T
    ): List<T> {
        log.atFine().log("Fetching expirable punishments for uuid=%s onlyActive=%s", punishedUuid, onlyActive)

        val now = ZonedDateTime.now()
        val query = if (onlyActive) {
            entityClass.find {
                punishedUuidFilter(table, punishedUuid) and activePunishmentFilter(table, now)
            }
        } else {
            entityClass.find {
                punishedUuidFilter(table, punishedUuid)
            }
        }

        return query.map { toApiObject(it) }
    }

    /**
     * Fetches punishments for a given player UUID without expiration filtering.
     *
     * @param entityClass DAO class of the punishment entity
     * @param table corresponding table for filtering
     * @param punishedUuid UUID of the punished player
     * @param toApiObject mapping from entity to API object
     * @return list of punishment API objects
     */
    suspend fun <E : AbstractPunishmentEntity, T> fetchPunishments(
        entityClass: AuditableLongEntityClass<E>,
        table: AbstractPunishmentTable,
        punishedUuid: UUID,
        toApiObject: (E) -> T
    ): List<T> {
        log.atFine().log("Fetching all punishments for uuid=%s", punishedUuid)
        return entityClass.find { punishedUuidFilter(table, punishedUuid) }.map { toApiObject(it) }
    }

    // endregion
    // region -- Internal helpers --

    /**
     * Creates note entities for a punishment.
     */
    private fun <T : LongEntity> createNotes(
        notes: List<String>,
        punishment: T,
        factory: (T, String) -> Unit
    ) {
        for (note in notes) {
            factory(punishment, note)
        }
    }


    /**
     * Builds a filter for active, unpunished and unexpired punishments.
     */
    private fun <T : AbstractUnpunishableExpirablePunishmentTable> activePunishmentFilter(
        table: T,
        now: ZonedDateTime
    ): Op<Boolean> = (table.unpunished eq false) and
            ((table.permanent eq true) or (table.expirationDate greater now))

    /**
     * Builds a filter for punishments by player UUID.
     */
    private fun <T : AbstractPunishmentTable> punishedUuidFilter(
        table: T,
        punishedUuid: UUID
    ): Op<Boolean> = (table.punishedUuid eq punishedUuid)

    // endregion
}