package dev.slne.surf.cloud.standalone.server.queue.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueueMetricsImplTest {
    @Test
    fun `counters increment correctly`() {
        val registry = SimpleMeterRegistry()
        val metrics = QueueMetricsImpl(registry)

        metrics.markEnqueued()
        metrics.markDequeued()
        metrics.markDequeued()

        assertEquals(1.0, metrics.enqueued())
        assertEquals(2.0, metrics.dequeued())
    }

}