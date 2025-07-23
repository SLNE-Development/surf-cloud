package dev.slne.surf.cloud.standalone.server.queue.timeout

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class ReentrantReadWriteLockQueueTimeoutRegistryTest {
    @Test
    fun `collectExpired returns and removes entries`() {
        val registry = ReentrantReadWriteLockQueueTimeoutRegistry()
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        registry.put(uuid1, 1000)
        registry.put(uuid2, 2000)

        val first = registry.collectExpired(1500).toSet()
        assertEquals(setOf(uuid1), first)

        val second = registry.collectExpired(2500).toSet()
        assertEquals(setOf(uuid2), second)
    }

    @Test
    fun `remove cancels timeout`() {
        val registry = ReentrantReadWriteLockQueueTimeoutRegistry()
        val uuid = UUID.randomUUID()
        registry.put(uuid, 1000)
        registry.remove(uuid)
        assertTrue(registry.collectExpired(2000).isEmpty())
    }

}