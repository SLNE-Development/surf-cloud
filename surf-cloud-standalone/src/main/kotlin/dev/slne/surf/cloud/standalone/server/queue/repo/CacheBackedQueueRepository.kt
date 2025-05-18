package dev.slne.surf.cloud.standalone.server.queue.repo

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.server.queue.BaseQueue
import dev.slne.surf.cloud.standalone.server.queue.GroupQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.QueueTarget
import dev.slne.surf.cloud.standalone.server.queue.ServerQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.cache.QueueCache
import org.springframework.stereotype.Component
import java.util.*

@Component
class CacheBackedQueueRepository(private val cache: QueueCache) : QueueRepository {
    override fun getServer(uid: Long): ServerQueueImpl =
        cache.getOrCreate(QueueTarget.Server(uid)) as ServerQueueImpl

    override fun getGroup(group: String): GroupQueueImpl =
        cache.getOrCreate(QueueTarget.Group(group)) as GroupQueueImpl

    override fun getServerOrNull(uid: Long): ServerQueueImpl? =
        cache.getOrNull(QueueTarget.Server(uid)) as? ServerQueueImpl

    override fun getGroupOrNull(group: String): GroupQueueImpl? =
        cache.getOrNull(QueueTarget.Group(group)) as? GroupQueueImpl

    override fun all(): Collection<BaseQueue<*>> = cache.cache.asMap().values
    override fun allServer(): Collection<ServerQueueImpl> =
        cache.cache.asMap().values.filterIsInstance<ServerQueueImpl>()

    override fun allGroup(): Collection<GroupQueueImpl> =
        cache.cache.asMap().values.filterIsInstance<GroupQueueImpl>()

    override suspend fun dequeueEverywhere(uuid: UUID, result: ConnectionResultEnum) {
        allGroup().forEach { queue ->
            queue.dequeue(uuid, result)
        }
    }
}