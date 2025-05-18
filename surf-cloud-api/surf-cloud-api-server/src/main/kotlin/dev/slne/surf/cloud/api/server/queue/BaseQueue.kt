package dev.slne.surf.cloud.api.server.queue

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import net.kyori.adventure.text.ComponentLike
import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface BaseQueue<Q : BaseQueue<Q>> : ComponentLike {
    val online: Boolean
    var suspended: Boolean

    suspend fun dequeue(uuid: UUID, reason: ConnectionResultEnum? = null)
    suspend fun size(): Int
    suspend fun isQueued(uuid: UUID): Boolean

    suspend fun peek(): QueueEntry?
    suspend fun getQueueName(): String
}