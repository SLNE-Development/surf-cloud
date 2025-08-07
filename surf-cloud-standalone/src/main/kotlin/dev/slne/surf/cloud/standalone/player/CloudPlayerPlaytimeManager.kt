package dev.slne.surf.cloud.standalone.player

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.coroutines.PlayerPlaytimeScope
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

@Component
class CloudPlayerPlaytimeManager(private val service: CloudPlayerService) : DisposableBean {
    private val log = logger()

    /** In-memory map of active sessions: player UUID -> session data */
    private val sessionsCache = Caffeine.newBuilder()
        .expireAfterAccess(10.minutes)
        .build<UUID, PlaytimeSession>()

    /**
     * Called every second to either increment existing session time or start a new session.
     * Database inserts happen outside the lock to avoid blocking other tasks.
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    suspend fun playtimeTask() {
        val toCreate = mutableObjectListOf<Pair<UUID, PlaytimeSession>>()
        val onlinePlayers = standalonePlayerManagerImpl.getRawOnlinePlayers()

        onlinePlayers.forEach { player ->
            val uuid = player.uuid
            val server = player.server ?: return@forEach
            val serverName = server.name
            val category = server.group

            val currentSession = sessionsCache.getIfPresent(uuid)

            // If server/category changed or there's no active session, create a new one
            if (currentSession == null || currentSession.serverName != serverName
                || !currentSession.category.equals(category, ignoreCase = true)) {
                // Flush old session if present
                if (currentSession != null) {
                    PlayerPlaytimeScope.launch { partialFlushSession(uuid, currentSession) }
                    sessionsCache.invalidate(uuid)
                }

                val newSession = PlaytimeSession(
                    sessionId = null,
                    serverName = serverName,
                    category = category,
                    startTime = ZonedDateTime.now()
                )

                sessionsCache.put(uuid, newSession)
                toCreate += uuid to newSession
            } else {
                if (player.afk) return@forEach
                // Just increment current session
                currentSession.accumulatedSeconds++
            }
        }

        // Insert new sessions into DB outside the lock
        PlayerPlaytimeScope.launch {
            val createdSessions = toCreate.map { (uuid, session) ->
                val dbId = createSessionInDB(uuid, session.serverName, session.category)
                Triple(uuid, session, dbId)
            }

            // Update sessionId in memory if still valid
            createdSessions.forEach { (uuid, session, dbId) ->
                if (sessionsCache.getIfPresent(uuid) === session) {
                    session.sessionId = dbId
                }
            }
        }
    }

    /**
     * Flushes ongoing sessions every 5 minutes to reduce data loss.
     */
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    suspend fun partialFlushAllTask() {
        val snapshot = mutableObjectListOf<Triple<UUID, Long, PlaytimeSession>>()
        val time = measureTimeMillis {
            // Take a snapshot of all sessions that are fully created in DB
            sessionsCache.asMap().forEach { (uuid, session) ->
                val sessionId = session.sessionId ?: return@forEach
                snapshot += Triple(uuid, sessionId, session)
            }

            // Perform DB updates outside the lock
            snapshot.forEach { (uuid, sessionId, session) ->
                service.updatePlaytimeInSession(uuid, sessionId, session.accumulatedSeconds)
            }
        }

        log.atInfo()
            .log("Flushed ${snapshot.size} playtime sessions to DB in $time ms")
    }

    /**
     * On player disconnect, remove session from memory and flush final time to DB.
     */
    @Suppress("unused")
    @CloudEventHandler
    fun onPlayerDisconnect(event: CloudPlayerDisconnectFromNetworkEvent) {
        val uuid = event.player.uuid
        val session = sessionsCache.asMap().remove(uuid) ?: return

        PlayerPlaytimeScope.launch { partialFlushSession(uuid, session) }
    }

    /**
     * On shutdown, flush all sessions to the database.
     */
    override fun destroy() = runBlocking {
        log.atInfo().log("Flushing all playtime sessions to DB on shutdown")
        val time = measureTimeMillis {
            sessionsCache.asMap().forEach { (playerId, session) ->
                partialFlushSession(playerId, session)
            }
            sessionsCache.invalidateAll()
        }
        log.atInfo().log("Flushed all playtime sessions to DB in $time ms")
    }

    /**
     * Creates a DB row for a new session and returns its ID
     */
    private suspend fun createSessionInDB(
        uuid: UUID,
        serverName: String,
        category: String
    ): Long {
        return service.createPlaytimeSession(uuid, serverName, category)
    }

    /**
     * Updates the DB row with the current accumulated playtime
     */
    private suspend fun partialFlushSession(playerId: UUID, session: PlaytimeSession) {
        val sessionId = session.sessionId ?: return
        service.updatePlaytimeInSession(playerId, sessionId, session.accumulatedSeconds)
    }

    suspend fun playtimeSessionFor(uuid: UUID) = sessionsCache.getIfPresent(uuid)

    data class PlaytimeSession(
        var sessionId: Long?,
        val serverName: String,
        val category: String,
        val startTime: ZonedDateTime,
        var accumulatedSeconds: Long = 0
    )
}