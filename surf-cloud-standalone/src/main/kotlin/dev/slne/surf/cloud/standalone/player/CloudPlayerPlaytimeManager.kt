package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.coroutines.PlayerPlaytimeScope
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Component
class CloudPlayerPlaytimeManager(private val service: CloudPlayerService) : DisposableBean {
    private val log = logger()

    /** In-memory map of active sessions: player UUID -> session data */
    private val sessions = mutableObject2ObjectMapOf<UUID, PlaytimeSession>()

    /** Protects read/write access to 'sessions' **/
    private val sessionsMutex = Mutex()

    /**
     * Called every second to either increment existing session time or start a new session.
     * Database inserts happen outside the lock to avoid blocking other tasks.
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    suspend fun playtimeTask() {
        val toCreate = mutableObjectListOf<Pair<UUID, PlaytimeSession>>()
        val onlinePlayers = standalonePlayerManagerImpl.getRawOnlinePlayers()

        // Acquire lock briefly to update in-memory sessions
        sessionsMutex.withLock {
            onlinePlayers.forEach { player ->
                val uuid = player.uuid
                val server = player.server ?: return@forEach
                val serverName = server.name
                val category = server.group

                val currentSession = sessions[uuid]

                // If server/category changed or there's no active session, create a new one
                if (currentSession == null || currentSession.serverName != serverName || currentSession.category != category) {
                    // Flush old session if present
                    if (currentSession != null) {
                        PlayerPlaytimeScope.launch { partialFlushSession(uuid, currentSession) }
                        sessions.remove(uuid)
                    }

                    val newSession = PlaytimeSession(
                        sessionId = null,
                        serverName = serverName,
                        category = category,
                        startTime = ZonedDateTime.now()
                    )

                    sessions[uuid] = newSession
                    toCreate += uuid to newSession
                } else {
                    // Just increment current session
                    currentSession.accumulatedSeconds++
                }
            }
        }

        // Insert new sessions into DB outside the lock
        PlayerPlaytimeScope.launch {
            val createdSessions = toCreate.map { (uuid, session) ->
                val dbId = createSessionInDB(uuid, session.serverName, session.category)
                Triple(uuid, session, dbId)
            }

            // Update sessionId in memory if still valid
            sessionsMutex.withLock {
                createdSessions.forEach { (uuid, session, dbId) ->
                    if (sessions[uuid] === session) {
                        session.sessionId = dbId
                    }
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
            sessionsMutex.withLock {
                sessions.forEach { (uuid, session) ->
                    val sessionId = session.sessionId ?: return@forEach
                    snapshot += Triple(uuid, sessionId, session)
                }
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
    @EventListener
    fun onPlayerDisconnect(event: CloudPlayerDisconnectFromNetworkEvent) {
        val uuid = event.player.uuid
        PlayerPlaytimeScope.launch {
            val session = sessionsMutex.withLock { sessions.remove(uuid) } ?: return@launch
            partialFlushSession(uuid, session)
        }
    }

    /**
     * On shutdown, flush all sessions to the database.
     */
    override fun destroy() = runBlocking {
        log.atInfo().log("Flushing all playtime sessions to DB on shutdown")
        val time = measureTimeMillis {
            sessionsMutex.withLock {
                sessions.forEach { (playerId, session) ->
                    partialFlushSession(playerId, session)
                }
                sessions.clear()
            }
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

    suspend fun playtimeSessionFor(uuid: UUID) = sessionsMutex.withLock { sessions[uuid] }

    data class PlaytimeSession(
        var sessionId: Long?,
        val serverName: String,
        val category: String,
        val startTime: ZonedDateTime,
        var accumulatedSeconds: Long = 0
    )
}