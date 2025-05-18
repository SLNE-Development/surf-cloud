package dev.slne.surf.cloud.standalone.server.queue.metrics

import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import org.springframework.stereotype.Component

@Component
class QueueMetricsRegistrar(
    metrics: QueueMetricsImpl,
    queues: QueueRepository
) {
    init {
        metrics.registerWaitingGauge(queues)
    }
}
