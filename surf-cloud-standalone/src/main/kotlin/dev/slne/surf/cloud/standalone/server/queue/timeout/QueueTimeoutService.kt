package dev.slne.surf.cloud.standalone.server.queue.timeout

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class QueueTimeoutService(
    private val timeouts: QueueTimeoutRegistry,
    private val queues: QueueRepository
) {

    fun register(uuid: UUID, timeoutSeconds: Long) =
        timeouts.put(uuid, System.currentTimeMillis() + timeoutSeconds * 1000)

    fun cancel(uuid: UUID) = timeouts.remove(uuid)

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    suspend fun scan() {
        val expired = timeouts.collectExpired(System.currentTimeMillis())
        expired.forEach { queues.dequeueEverywhere(it, ConnectionResultEnum.DISCONNECTED) }
    }
}