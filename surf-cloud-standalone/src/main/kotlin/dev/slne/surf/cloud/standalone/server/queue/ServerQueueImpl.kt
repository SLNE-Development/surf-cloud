package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.util.getValue
import dev.slne.surf.cloud.api.common.util.setValue
import dev.slne.surf.cloud.api.server.queue.QueueEntry
import dev.slne.surf.cloud.api.server.queue.ServerQueue
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ServerQueueImpl(
    override val serverName: String,
    var latestGroup: String,
    private val queues: QueueRepository,
) : ServerQueue {

    val groupQueue get() = queues.getGroup(latestGroup)

    override var suspended: Boolean by AtomicBoolean()

    override val online get() = serverManagerImpl.retrieveServerByName(serverName) != null

    override suspend fun dequeue(
        uuid: UUID,
        reason: ConnectionResultEnum?
    ) {
        groupQueue.dequeue(uuid, reason, serverName)
    }

    override suspend fun size(): Int {
        return groupQueue.size(serverName)
    }

    override suspend fun isQueued(uuid: UUID): Boolean {
        return groupQueue.isQueued(uuid, serverName)
    }

    override suspend fun peek(): QueueEntry? {
        return groupQueue.peek(serverName)
    }

    suspend fun queue(
        uuid: UUID,
        priority: Int,
        bypassFull: Boolean,
        bypassQueue: Boolean
    ) = groupQueue.queue(
        uuid = uuid,
        priority = priority,
        bypassFull = bypassFull,
        bypassQueue = bypassQueue,
        preferredServerName = serverName
    )

    override suspend fun getQueueName() = serverName

    override fun asComponent(): Component {
        TODO("Not yet implemented")
    }
}