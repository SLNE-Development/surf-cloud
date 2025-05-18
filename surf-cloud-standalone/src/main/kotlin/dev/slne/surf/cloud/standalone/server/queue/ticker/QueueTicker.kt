package dev.slne.surf.cloud.standalone.server.queue.ticker

import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class QueueTicker(private val queues: QueueRepository) {

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    suspend fun tick(): Unit = coroutineScope {
        queues.allGroup()
            .filter { it.online }
            .map { queue -> async { queue.processFrontAndDisplay() } }
            .awaitAll()
    }
}