package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.util.getValue
import dev.slne.surf.cloud.api.common.util.setValue
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import kotlinx.coroutines.runBlocking
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class QueueLifecycleService(private val queueRepo: QueueRepository) : SmartLifecycle {
    private var running by AtomicBoolean()
    override fun isRunning() = running

    override fun start() {
        running = true
    }

    override fun stop() {
        runBlocking { queueRepo.allGroup().forEach { it.stop(null) } }
        running = false
    }
}