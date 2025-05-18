package dev.slne.surf.cloud.standalone.server.queue.timeout

import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write


@Component
class ReentrantRedWriteLockQueueTimeoutRegistry : QueueTimeoutRegistry {
    private val map = mutableObject2LongMapOf<UUID>().apply { defaultReturnValue(-1) }
    private val lock = ReentrantReadWriteLock()

    override fun put(uuid: UUID, untilMillis: Long) = lock.write { map[uuid] = untilMillis }
    override fun remove(uuid: UUID): Unit = lock.write { map.removeLong(uuid) }
    override fun collectExpired(now: Long) = lock.write {
        map.object2LongEntrySet().fastIterator().asSequence()
            .filter { it.longValue <= now }
            .map { it.key }
            .toObjectSet()
            .also {
                it.forEach { uuid -> map.removeLong(uuid) }
            }
    }
}