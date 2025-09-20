package dev.slne.surf.cloud.standalone.server.queue.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.benmanes.caffeine.cache.Scheduler
import com.sksamuel.aedile.core.expireAfterAccess
import com.sksamuel.aedile.core.withRemovalListener
import dev.slne.surf.cloud.api.server.queue.BaseQueue
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.cloud.standalone.server.queue.GroupQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.QueueTarget
import dev.slne.surf.cloud.standalone.server.queue.ServerQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.metrics.QueueMetricsImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class QueueCache(
    private val metrics: QueueMetricsImpl,
    @param:Lazy private val queueRepo: QueueRepository,
    configHolder: StandaloneConfigHolder
) {

    val cache: LoadingCache<QueueTarget<*>, BaseQueue<*>> = Caffeine.newBuilder()
        .expireAfterAccess(configHolder.config.queue.cacheRetainMinutes.minutes)
        .scheduler(Scheduler.systemScheduler())
        .withRemovalListener { _, queue, _ ->
            if (queue is GroupQueueImpl) {
                queue.stop(null)
            } else if (queue is ServerQueueImpl) {
                queue.groupQueue.stop(queue.serverName)
            }
        }
        .build { it.createQueue(queueRepo, metrics) }

    @Suppress("UNCHECKED_CAST")
    fun <Q : BaseQueue<Q>> getOrCreate(target: QueueTarget<Q>): Q {
        return cache.get(target) as Q
    }

    fun getOrNull(target: QueueTarget<*>): BaseQueue<*>? {
        return cache.getIfPresent(target)
    }

    fun all(): List<BaseQueue<*>> {
        return cache.asMap().values.toList()
    }
}