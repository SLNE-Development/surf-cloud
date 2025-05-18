package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.server.queue.BaseQueue
import dev.slne.surf.cloud.api.server.queue.GroupQueue
import dev.slne.surf.cloud.api.server.queue.ServerQueue
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.metrics.QueueMetricsImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import dev.slne.surf.cloud.standalone.server.serverManagerImpl

interface QueueTarget<Q : BaseQueue<Q>> {
    val id: String
    fun createQueue(
        queueRepo: QueueRepository,
        metrics: QueueMetricsImpl
    ): Q

    data class Server(val uid: Long) : QueueTarget<ServerQueue> {
        override val id = "server:$uid"
        override fun createQueue(
            queueRepo: QueueRepository,
            metrics: QueueMetricsImpl
        ): ServerQueueImpl = ServerQueueImpl(
            uid,
            (serverManagerImpl.getServerByIdUnsafe(uid) as? StandaloneCloudServerImpl)?.group
                ?: CloudProperties.SERVER_CATEGORY_NOT_SET,
            queueRepo
        )
    }

    data class Group(val group: String) : QueueTarget<GroupQueue> {
        override val id = "group:$group"
        override fun createQueue(
            queueRepo: QueueRepository,
            metrics: QueueMetricsImpl
        ): GroupQueueImpl = GroupQueueImpl(group, metrics, queueRepo)
    }
}