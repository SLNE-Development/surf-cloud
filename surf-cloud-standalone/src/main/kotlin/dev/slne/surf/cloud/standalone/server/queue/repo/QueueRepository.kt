package dev.slne.surf.cloud.standalone.server.queue.repo

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.server.queue.BaseQueue
import dev.slne.surf.cloud.standalone.server.queue.GroupQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.ServerQueueImpl
import java.util.*

interface QueueRepository {
    fun getServer(serverName: String): ServerQueueImpl
    fun getGroup(group: String): GroupQueueImpl

    fun getServerOrNull(serverName: String): ServerQueueImpl?
    fun getGroupOrNull(group: String): GroupQueueImpl?

    fun all(): Collection<BaseQueue<*>>
    fun allServer(): Collection<ServerQueueImpl>
    fun allGroup(): Collection<GroupQueueImpl>

    suspend fun dequeueEverywhere(uuid: UUID, result: ConnectionResultEnum)
}