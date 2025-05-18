package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.util.queue.FastFairPriorityQueue
import dev.slne.surf.cloud.api.server.queue.GroupQueue
import dev.slne.surf.cloud.core.common.coroutines.QueueConnectionScope
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.display.ServerQueueDisplay
import dev.slne.surf.cloud.standalone.server.queue.entry.PlayerQueueHandle
import dev.slne.surf.cloud.standalone.server.queue.entry.QueueEntryImpl
import dev.slne.surf.cloud.standalone.server.queue.metrics.QueueMetricsImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import java.util.*

class GroupQueueImpl(
    override val group: String,
    private val metrics: QueueMetricsImpl?,
    private val queues: QueueRepository
) :
    GroupQueue {
    private val entries: FastFairPriorityQueue<QueueEntryImpl> =
        FastFairPriorityQueue(compareBy { it })
    private val lock = Mutex()

    val unsafeSize get() = entries.size

    @Volatile
    override var suspended = false

    override val online: Boolean
        get() = serverManagerImpl.existsServerGroup(group)

    val display = ServerQueueDisplay(this, queues)

    override suspend fun dequeue(uuid: UUID, reason: ConnectionResultEnum?) {
        dequeue(uuid, reason, null)
    }

    suspend fun dequeue(uuid: UUID, reason: ConnectionResultEnum?, serverUid: Long?) {
        lock.withLock {
            val iterator = entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val handle = entry.handle

                if (handle.uuid == uuid && (serverUid == null || entry.preferredServerUid == serverUid)) {
                    iterator.remove()
                    if (reason == null) {
                        handle.cancel()
                    } else {
                        handle.complete(reason)
                    }
                    metrics?.markDequeued()
                }
            }
        }
    }

    override suspend fun peek(): QueueEntryImpl? = lock.withLock { entries.peek() }

    suspend fun peek(preferredServerUid: Long): QueueEntryImpl? =
        lock.withLock { entries.firstOrNull { it.preferredServerUid == preferredServerUid } }

    override suspend fun size() = lock.withLock { entries.size }
    suspend fun size(preferredServerUid: Long): Int =
        lock.withLock { entries.count { it.preferredServerUid == preferredServerUid } }

    override suspend fun isQueued(uuid: UUID): Boolean =
        lock.withLock { entries.any { it.handle.uuid == uuid } }

    suspend fun isQueued(uuid: UUID, preferredServerUid: Long): Boolean =
        lock.withLock { entries.any { it.handle.uuid == uuid && it.preferredServerUid == preferredServerUid } }

    override suspend fun getQueueName(): String {
        return group
    }

    suspend fun queue(
        uuid: UUID,
        priority: Int,
        bypassFull: Boolean,
        bypassQueue: Boolean,
        preferredServerUid: Long? = null
    ): Deferred<ConnectionResultEnum> {
        val handle = PlayerQueueHandle(uuid)
        val entry = QueueEntryImpl(handle, priority, bypassFull, bypassQueue, preferredServerUid)

        if (bypassQueue && bypassFull) {
            QueueConnectionScope.launch {
                while (isActive && entry.hasConnectionAttemptsLeft()) {
                    if (entry.awaitingConnection) {
                        delay(500)
                    }
                    sendDirect(entry)?.join()
                }
            }

            return handle.result
        }

        if (bypassQueue) entry.priority = Int.MAX_VALUE

        lock.withLock { entries.add(entry) }
        metrics?.markEnqueued()
        return handle.result
    }

    private fun sendDirect(entry: QueueEntryImpl): Job? {
        if (entry.awaitingConnection) return null
        entry.awaitingConnection = true

        return QueueConnectionScope.launch {
            val player = entry.handle.uuid.toCloudPlayer() ?: return@launch dequeue(
                entry.handle.uuid,
                ConnectionResultEnum.DISCONNECTED
            )

            val preferredServerUid = entry.preferredServerUid
            val result = if (preferredServerUid != null) {
                (serverManagerImpl.retrieveServerById(preferredServerUid) as? StandaloneCloudServerImpl)?.let { server ->
                    player.connectToServer(server)
                }
            } else {
                player.connectToServer(group)
            } ?: return@launch dequeue(entry.handle.uuid, ConnectionResultEnum.SERVER_OFFLINE)

            val success = result.isSuccess
            if (success) {
                dequeue(player.uuid, result)
            } else {
                entry.connectionAttempts++
                entry.awaitingConnection = false

                if (!entry.hasConnectionAttemptsLeft()) {
                    dequeue(
                        player.uuid,
                        ConnectionResultEnum.MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED(
                            result,
                            getQueueName(),
                            standaloneConfig.queue.maxConnectionAttempts
                        ),
                        preferredServerUid
                    )
                }
            }
        }
    }

    suspend fun processFrontAndDisplay() {
        coroutineScope {
            launch {
                display.tickDisplay()
            }
        }

        if (suspended) return

        var entry = peek()?.takeUnless { !it.bypassFull && isFull(it.preferredServerUid) } ?: return
        val preferredServerUid = entry.preferredServerUid

        if (preferredServerUid != null) {
            val serverQueue = queues.getServerOrNull(preferredServerUid)
            if (serverQueue != null) {
                if (serverQueue.suspended) {
                    entry =
                        entries.firstOrNull { it.preferredServerUid == null || it.preferredServerUid != preferredServerUid }
                            ?: return
                }
            }
        }

        if (entry.handle.uuid.toCloudPlayer() == null) {
            dequeue(entry.handle.uuid, ConnectionResultEnum.DISCONNECTED) // Player is offline
            return
        }

        if (isFull(preferredServerUid)) return
        sendDirect(entry)
    }

    suspend fun isFull(preferredServerUid: Long?): Boolean {
        if (preferredServerUid != null) {
            val server =
                serverManagerImpl.retrieveServerById(preferredServerUid) as? StandaloneCloudServerImpl
                    ?: return true
            return !server.hasEmptySlots()
        }

        val servers = serverManagerImpl.retrieveServersInGroup(group)
        return servers.all { !it.hasEmptySlots() }
    }

    suspend fun stop(preferredServerUid: Long?) {
        lock.withLock {
            if (preferredServerUid != null) {
                val iterator = entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.preferredServerUid == preferredServerUid) {
                        iterator.remove()
                        entry.handle.cancel()
                    }
                }
            } else {
                entries.forEach { it.handle.cancel() }
                entries.clear()
            }
        }
    }

    suspend fun entriesSnapshot() = lock.withLock { entries.snapshot() }

    override fun asComponent(): Component {
        TODO("Not yet implemented")
    }
}