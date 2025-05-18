package dev.slne.surf.cloud.api.server.queue

import dev.slne.surf.cloud.api.common.CloudInstance
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface QueueMetrics {
    fun enqueued(): Double
    fun dequeued(): Double

    companion object : QueueMetrics by CloudInstance.getBean(QueueMetrics::class.java)
}