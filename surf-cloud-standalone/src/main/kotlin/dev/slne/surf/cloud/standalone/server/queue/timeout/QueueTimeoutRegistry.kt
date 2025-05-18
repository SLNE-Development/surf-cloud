package dev.slne.surf.cloud.standalone.server.queue.timeout

import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

interface QueueTimeoutRegistry {
    fun put(uuid: UUID, untilMillis: Long)
    fun remove(uuid: UUID)
    fun collectExpired(now: Long): ObjectSet<UUID>
}