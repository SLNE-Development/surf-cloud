package dev.slne.surf.cloud.standalone.server.queue.metrics

import dev.slne.surf.cloud.api.server.queue.QueueMetrics
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class QueueMetricsImpl(
    private val registry: MeterRegistry
) : QueueMetrics {
    private val enqueued = Counter.builder("cloud.queue.enqueued").register(registry)
    private val dequeued = Counter.builder("cloud.queue.dequeued").register(registry)

    fun markEnqueued() = enqueued.increment()
    fun markDequeued() = dequeued.increment()

    fun registerWaitingGauge(queues: QueueRepository) {
        Gauge.builder("cloud.queue.waiting") {
            queues.allGroup().sumOf { it.unsafeSize }.toDouble()
        }.register(registry)
    }

    override fun enqueued(): Double {
        return enqueued.count()
    }

    override fun dequeued(): Double {
        return dequeued.count()
    }
}