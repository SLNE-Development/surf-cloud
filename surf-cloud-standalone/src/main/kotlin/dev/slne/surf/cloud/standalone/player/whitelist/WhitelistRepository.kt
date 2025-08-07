package dev.slne.surf.cloud.standalone.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.util.singleOrNullOrThrow
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.cloud.api.server.plugin.NotTransactional
import dev.slne.surf.cloud.core.common.player.whitelist.MutableWhitelistEntryImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl
import dev.slne.surf.cloud.standalone.exposed.CurrentDbProvider
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.cloud.standalone.player.db.exposed.whitelist.WhitelistEntity
import dev.slne.surf.cloud.standalone.player.db.exposed.whitelist.WhitelistTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.transform
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Service
import java.util.*

@Service
@CoroutineTransactional
class WhitelistRepository(
    private val playerService: CloudPlayerService,
    private val dbProvider: CurrentDbProvider
) {

    suspend fun whitelistStatus(
        uuid: UUID,
        group: String?,
        server: String?
    ): WhitelistStatusResult? {
        val baseQuery = WhitelistTable.select(WhitelistTable.blocked)
            .where { WhitelistTable.uuid eq uuid }

        val filteredQuery = when {
            group != null -> baseQuery.andWhere { WhitelistTable.group eq group }
            server != null -> baseQuery.andWhere { WhitelistTable.serverName eq server }
            else -> error("Either group or server must be provided")
        }

        return filteredQuery.singleOrNullOrThrow()
            ?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }
    }

    /**
     * Fetches whitelist rows in batches of [batchSize] and streams the result
     * back as `(UUID, List<WhitelistStatusResult?>)`.
     *
     * * Exactly **one** SQL query per batch.
     * * Runs inside `Dispatchers.IO`.
     * * `@NotTransactional` → caller controls transaction scope.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @NotTransactional
    suspend fun whitelistStatusBatched(
        players: Flow<Pair<UUID, Pair<WhitelistEntry.ServerName, WhitelistEntry.Group>>>,
        batchSize: Int = 1_000
    ): Flow<Pair<UUID, List<WhitelistStatusResult?>>> = players.chunked(batchSize)
        .transform { chunk ->
            if (chunk.isEmpty()) return@transform
            val uuids = chunk.map { it.first }

            newSuspendedTransaction(Dispatchers.IO, dbProvider.current) {
                val rowsByUuid = WhitelistTable
                    .select(
                        WhitelistTable.uuid,
                        WhitelistTable.blocked,
                        WhitelistTable.serverName,
                        WhitelistTable.group
                    )
                    .where { WhitelistTable.uuid inList uuids }
                    .groupBy { it[WhitelistTable.uuid] }

                for ((uuid, serverAndGroup) in chunk) {
                    val (serverName, group) = serverAndGroup
                    val playerRows = rowsByUuid[uuid].orEmpty()

                    /** Looks for the first row matching `server` or, as fallback, `group`. */
                    fun findRow(
                        server: String,
                        group: String
                    ): WhitelistStatusResult? =
                        playerRows.firstOrNull {
                            it[WhitelistTable.serverName].equals(server, ignoreCase = true)
                        }?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }
                            ?: playerRows.firstOrNull {
                                it[WhitelistTable.group].equals(group, ignoreCase = true)
                            }?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }

                    val serverStatus = findRow(serverName, group)
                    val groupStatus  = playerRows.firstOrNull {
                        it[WhitelistTable.group].equals(group, ignoreCase = true)
                    }?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }

                    emit(uuid to listOf(serverStatus, groupStatus))
                }
            }
        }
        .buffer()  // overlap DB & downstream


    suspend fun getWhitelist(uuid: UUID, group: String?, server: String?): WhitelistEntryImpl? {
        val baseQuery = WhitelistTable.selectAll().where { WhitelistTable.uuid eq uuid }

        val filteredQuery = when {
            group != null -> baseQuery.andWhere { WhitelistTable.group eq group }
            server != null -> baseQuery.andWhere { WhitelistTable.serverName eq server }
            else -> error("Either group or server must be provided")
        }

        val result = filteredQuery.singleOrNullOrThrow() ?: return null

        return WhitelistEntryImpl(
            uuid = result[WhitelistTable.uuid],
            blocked = result[WhitelistTable.blocked],
            group = result[WhitelistTable.group],
            serverName = result[WhitelistTable.serverName],
            createdAt = result[WhitelistTable.createdAt],
            updatedAt = result[WhitelistTable.updatedAt]
        )
    }

    suspend fun createWhitelist(whitelist: WhitelistEntryImpl): WhitelistEntryImpl? {
        if (whitelistStatus(whitelist.uuid, whitelist.group, whitelist.serverName) != null) {
            return null
        }

        val player = playerService.findByUuid(whitelist.uuid)
            ?: error("Player with UUID ${whitelist.uuid} not found")
        val entity = WhitelistEntity.new {
            uuid = whitelist.uuid
            blocked = whitelist.blocked
            group = whitelist.group
            serverName = whitelist.serverName
            this.cloudPlayer = player
        }

        return WhitelistEntryImpl(
            uuid = entity.uuid,
            blocked = entity.blocked,
            group = entity.group,
            serverName = entity.serverName,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    suspend fun editWhitelist(
        uuid: UUID,
        group: String?,
        server: String?,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        val baseQuery = WhitelistTable.selectAll().forUpdate().where { WhitelistTable.uuid eq uuid }
        val filteredQuery = when {
            group != null -> baseQuery.andWhere { WhitelistTable.group eq group }
            server != null -> baseQuery.andWhere { WhitelistTable.serverName eq server }
            else -> error("Either group or server must be provided")
        }

        val result = filteredQuery.singleOrNullOrThrow() ?: return false
        val entity = WhitelistEntity.wrapRow(result)

        val mutableEntry = MutableWhitelistEntryImpl(
            uuid = entity.uuid,
            blocked = entity.blocked,
            group = entity.group,
            serverName = entity.serverName,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

        val edited = mutableEntry.copy()
        edited.edit()

        entity.blocked = edited.blocked
        entity.group = edited.group
        entity.serverName = edited.serverName

        return mutableEntry != edited
    }

    suspend fun updateWhitelist(updated: MutableWhitelistEntryImpl): Boolean {
        val baseQuery =
            WhitelistTable.selectAll().forUpdate().where { WhitelistTable.uuid eq updated.uuid }
        val filteredQuery = when {
            updated.group != null -> baseQuery.andWhere { WhitelistTable.group eq updated.group }
            updated.serverName != null -> baseQuery.andWhere { WhitelistTable.serverName eq updated.serverName }
            else -> error("Either group or server must be provided")
        }

        val result = filteredQuery.singleOrNullOrThrow() ?: return false
        val entity = WhitelistEntity.wrapRow(result)

        entity.blocked = updated.blocked
        entity.group = updated.group
        entity.serverName = updated.serverName

        return entity.flush()
    }


    @JvmInline
    value class WhitelistStatusResult(val blocked: Boolean)
}

private fun List<WhitelistRepository.WhitelistStatusResult?>.toWhitelistStatus(): WhitelistStatus {
    var hasBlocked = false
    var hasAllowed = false
    for (entry in this) {
        when (entry?.blocked) {
            true -> hasBlocked = true
            false -> hasAllowed = true
            null -> continue
        }
        if (hasBlocked && hasAllowed) return WhitelistStatus.UNKNOWN
    }
    return when {
        hasBlocked -> WhitelistStatus.BLOCKED
        hasAllowed -> WhitelistStatus.ACTIVE
        else -> WhitelistStatus.NONE
    }
}