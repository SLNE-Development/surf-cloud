package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import kotlinx.coroutines.runBlocking
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class QueueLifecycleService(private val queueRepo: QueueRepository) : SmartLifecycle {
    @Volatile
    private var running = false
    override fun isRunning() = running


    override fun start() {
        running = true
    }

    override fun stop() {
        runBlocking { queueRepo.allGroup().forEach { it.stop(null) } }
        running = false
    }
}