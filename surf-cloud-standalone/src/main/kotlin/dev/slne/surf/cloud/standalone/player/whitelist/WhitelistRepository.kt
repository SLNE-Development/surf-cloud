package dev.slne.surf.cloud.standalone.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.util.Either
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
import org.jetbrains.exposed.sql.Query
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
        groupOrServerName: Either<String, String>
    ): WhitelistStatusResult? {
        val playerId = playerService.findIdByUuid(uuid) ?: return null

        return WhitelistTable.select(WhitelistTable.blocked)
            .where { WhitelistTable.cloudPlayerId eq playerId }
            .filterByGroupOrServer(groupOrServerName)
            .singleOrNullOrThrow()
            ?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }
    }


    /**
     * Fetches whitelist rows in batches of [batchSize] and streams the result
     * back as `(UUID, List<WhitelistStatusResult?>)`.
     *
     * * Exactly **one** SQL query per batch.
     * * Runs inside `Dispatchers.IO`.
     * * `@NotTransactional` -> caller controls transaction scope.
     *
     * @param players A flow of pairs containing the player's UUID and a pair of server name and group.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @NotTransactional
    suspend fun whitelistStatusBatched(
        players: Flow<Pair<UUID, Pair< /*server name*/ String, /*group*/ String>>>,
        batchSize: Int = 1_000
    ): Flow<Pair<UUID, List<WhitelistStatusResult?>>> = players.chunked(batchSize)
        .transform { chunk ->
            if (chunk.isEmpty()) return@transform
            val uuids = chunk.map { it.first }

            newSuspendedTransaction(Dispatchers.IO, dbProvider.current) {
                val uuidToId = playerService.findIdsByUuids(uuids)
                val ids = uuidToId.values

                val rowsByUuid = WhitelistTable
                    .select(
                        WhitelistTable.cloudPlayerId,
                        WhitelistTable.blocked,
                        WhitelistTable.serverName,
                        WhitelistTable.group
                    )
                    .where { WhitelistTable.cloudPlayerId inList ids }
                    .groupBy { it[WhitelistTable.cloudPlayerId] }

                for ((uuid, serverAndGroup) in chunk) {
                    val (serverName, group) = serverAndGroup
                    val id = uuidToId[uuid] ?: continue
                    val playerRows = rowsByUuid[id].orEmpty()

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
                    val groupStatus = playerRows.firstOrNull {
                        it[WhitelistTable.group].equals(group, ignoreCase = true)
                    }?.let { WhitelistStatusResult(it[WhitelistTable.blocked]) }

                    emit(uuid to listOf(serverStatus, groupStatus))
                }
            }
        }
        .buffer()  // overlap DB & downstream


    suspend fun getWhitelist(
        uuid: UUID,
        groupOrServerName: Either<String, String>
    ): WhitelistEntryImpl? {
        val id = playerService.findIdByUuid(uuid) ?: return null
        return WhitelistTable.selectAll()
            .where { WhitelistTable.cloudPlayerId eq id }
            .filterByGroupOrServer(groupOrServerName)
            .singleOrNullOrThrow()
            ?.let { result ->
                WhitelistEntryImpl(
                    uuid = uuid,
                    blocked = result[WhitelistTable.blocked],
                    groupOrServerName = Either.of(
                        result[WhitelistTable.group],
                        result[WhitelistTable.serverName]
                    ),
                    createdAt = result[WhitelistTable.createdAt],
                    updatedAt = result[WhitelistTable.updatedAt]
                )
            }
    }


    suspend fun createWhitelist(whitelist: WhitelistEntryImpl): WhitelistEntryImpl? {
        if (whitelistStatus(whitelist.uuid, whitelist.groupOrServerName) != null) {
            return null
        }

        val player = playerService.findByUuid(whitelist.uuid)
            ?: error("Player with UUID ${whitelist.uuid} not found")
        val entity = WhitelistEntity.new {
            blocked = whitelist.blocked
            group = whitelist.groupOrServerName.leftOrNull()
            serverName = whitelist.groupOrServerName.rightOrNull()
            this.cloudPlayer = player
        }

        return WhitelistEntryImpl(
            uuid = whitelist.uuid,
            blocked = entity.blocked,
            groupOrServerName = Either.of(
                entity.group,
                entity.serverName
            ),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    suspend fun editWhitelist(
        uuid: UUID,
        groupOrServerName: Either<String, String>,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        val id = playerService.findIdByUuid(uuid) ?: return false
        val entity = WhitelistTable.selectAll()
            .forUpdate()
            .where { WhitelistTable.cloudPlayerId eq id }
            .filterByGroupOrServer(groupOrServerName)
            .singleOrNullOrThrow()
            ?.let { WhitelistEntity.wrapRow(it) }
            ?: return false

        val mutableEntry = MutableWhitelistEntryImpl(
            uuid = uuid,
            blocked = entity.blocked,
            groupOrServerName = Either.of(
                entity.group,
                entity.serverName
            ),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

        val edited = mutableEntry.copy()
        edited.edit()

        entity.blocked = edited.blocked
        entity.group = edited.groupOrServerName.leftOrNull()
        entity.serverName = edited.groupOrServerName.rightOrNull()

        return mutableEntry != edited
    }

    suspend fun updateWhitelist(updated: MutableWhitelistEntryImpl): Boolean {
        val playerId = playerService.findIdByUuid(updated.uuid) ?: return false
        val entity = WhitelistTable.selectAll()
            .forUpdate()
            .where { WhitelistTable.cloudPlayerId eq playerId }
            .filterByGroupOrServer(updated.groupOrServerName)
            .singleOrNullOrThrow()
            ?.let { WhitelistEntity.wrapRow(it) }
            ?: return false

        entity.blocked = updated.blocked
        entity.group = updated.groupOrServerName.leftOrNull()
        entity.serverName = updated.groupOrServerName.rightOrNull()

        return entity.flush()
    }


    @JvmInline
    value class WhitelistStatusResult(val blocked: Boolean)
}

private fun Query.filterByGroupOrServer(groupOrServerName: Either<String, String>) =
    groupOrServerName.fold({ group ->
        andWhere { WhitelistTable.group like group }
    }, { server ->
        andWhere { WhitelistTable.serverName like server }
    })