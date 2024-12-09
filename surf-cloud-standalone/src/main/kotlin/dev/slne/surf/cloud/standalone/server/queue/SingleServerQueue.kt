package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum.*
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.queue.FairSuspendPriorityQueue
import dev.slne.surf.cloud.api.common.util.toObjectList
import dev.slne.surf.cloud.core.common.coroutines.QueueProcessingScope
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val QUEUE_PRIORITY_KEY = "queue_priority"

class SingleServerQueue(private val server: StandaloneCloudServerImpl) {
    private val log = logger()
    private val queue =
        FairSuspendPriorityQueue<StandaloneCloudPlayerImpl> { a, b ->
            val aPriority = a.getLuckpermsMetaData(QUEUE_PRIORITY_KEY)?.toIntOrNull() ?: -1
            val bPriority = b.getLuckpermsMetaData(QUEUE_PRIORITY_KEY)?.toIntOrNull() ?: -1
            bPriority.compareTo(aPriority)
        }

    private val mutex = Mutex()

    @Volatile
    private var processing = false

    val queueDisplay = ServerQueueDisplay(this)

    private suspend fun processQueue() {
        if (processing) return
        processing = true

        while (true) {
            val player = mutex.withLock {
                if (queue.isEmpty() || !server.hasEmptySlots()) return
                queue.poll()
            } ?: break // No more players to process at the moment
            val rawResult = player.connectToServer(server) // suspend
            val (result, message) = rawResult

            runCatching {
                when (result) {
                    // Re-add the player to the queue if the server is full
                    SERVER_FULL -> mutex.withLock { queue.addFirst(player) }
                    CATEGORY_FULL -> throw AssertionError("How did that happen? We are not using categories here.")
                    SERVER_NOT_FOUND -> throw AssertionError("How did this pop up here? The server should exist.")
                    SERVER_OFFLINE -> throw AssertionError("Some very weird stuff happened here. The server should be online.")
                    ALREADY_CONNECTED -> log.atWarning()
                        .log("Player %s is already connected to the server", player.uuid)

                    CANNOT_SWITCH_PROXY -> throw AssertionError("It is not possible to change the proxy here.")
                    OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET, CANNOT_COMMUNICATE_WITH_PROXY, CANNOT_CONNECT_TO_PROXY ->
                        throw AssertionError("Well this check should have been done before adding the player to the queue. Someone definitely messed up and it was not me.")

                    CONNECTION_IN_PROGRESS -> Unit // Don't know what happened here, but good for us
                    CONNECTION_CANCELLED, SERVER_DISCONNECTED -> {
                        if (message != null) {
                            player.sendMessage(message)
                        }
                    }

                    SUCCESS -> player.connectionQueueCallback?.complete(rawResult)
                }
            }.onFailure { player.connectionQueueCallback?.completeExceptionally(it) }
        }

        processing = false
    }

    suspend fun addPlayerToQueue(player: StandaloneCloudPlayerImpl) {
        mutex.withLock {
            if (!queue.contains(player)) {
                queue.offer(player)
            }
        }

        QueueProcessingScope.launch { processQueue() }
    }

    /**
     * Called when a player leaves the network.
     *
     * @param player The player that left the network.
     */
    suspend fun handlePlayerLeave(player: CloudPlayer) {
        if (player !is StandaloneCloudPlayerImpl) return

        mutex.withLock { queue.remove(player) }
        QueueProcessingScope.launch { processQueue() }
    }

    /**
     * Removes a player from the queue.
     *
     * @param player The player to remove from the queue.
     */
    suspend fun removePlayerFromQueue(player: StandaloneCloudPlayerImpl) {
        mutex.withLock { queue.remove(player) }
        QueueProcessingScope.launch { processQueue() }
    }

    /**
     * Called when the server information is updated. E.g., the maximum player count changes.
     */
    fun handleServerUpdate() {
        QueueProcessingScope.launch { processQueue() }
    }

    suspend fun snapshot() = mutex.withLock { queue.toObjectList() }
}