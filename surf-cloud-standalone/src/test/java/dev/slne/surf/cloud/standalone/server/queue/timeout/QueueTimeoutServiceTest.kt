package dev.slne.surf.cloud.standalone.server.queue.timeout

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.server.queue.BaseQueue
import dev.slne.surf.cloud.standalone.server.queue.GroupQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.ServerQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class QueueTimeoutServiceTest {

    @Test
    fun `register stores absolute expiration`() {
        val registry = FakeQueueTimeoutRegistry()
        val repository = FakeQueueRepository()
        val service = QueueTimeoutService(registry, repository)
        val uuid = UUID.randomUUID()

        val before = System.currentTimeMillis()
        service.register(uuid, 2)
        val after = System.currentTimeMillis()

        val (recordedUuid, recordedUntil) = registry.putCalls.single()
        assertEquals(uuid, recordedUuid)

        val expectedMin = before + 2_000
        val expectedMax = after + 2_000
        assertTrue(
            recordedUntil in expectedMin..expectedMax,
            "Timeout should be stored as an absolute timestamp"
        )
    }

    @Test
    fun `cancel removes registered timeout`() {
        val registry = FakeQueueTimeoutRegistry()
        val repository = FakeQueueRepository()
        val service = QueueTimeoutService(registry, repository)
        val uuid = UUID.randomUUID()

        service.cancel(uuid)

        assertEquals(listOf(uuid), registry.removeCalls)
    }

    @Test
    fun `scan dequeues expired players`() = runBlocking {
        val registry = FakeQueueTimeoutRegistry()
        val repository = FakeQueueRepository()
        val service = QueueTimeoutService(registry, repository)

        val expired = listOf(UUID.randomUUID(), UUID.randomUUID())
        registry.expiredToReturn = ObjectOpenHashSet(expired)

        service.scan()

        assertIterableEquals(
            expired.map { it to ConnectionResultEnum.DISCONNECTED },
            repository.dequeueCalls
        )
        assertTrue(registry.lastCollectNow != null)
    }

}

private class FakeQueueTimeoutRegistry : QueueTimeoutRegistry {
    val putCalls = mutableListOf<Pair<UUID, Long>>()
    val removeCalls = mutableListOf<UUID>()
    var expiredToReturn: ObjectOpenHashSet<UUID> = ObjectOpenHashSet()
    var lastCollectNow: Long? = null

    override fun put(uuid: UUID, untilMillis: Long) {
        putCalls += uuid to untilMillis
    }

    override fun remove(uuid: UUID) {
        removeCalls += uuid
    }

    override fun collectExpired(now: Long): ObjectOpenHashSet<UUID> {
        lastCollectNow = now
        return ObjectOpenHashSet(expiredToReturn)
    }
}

private class FakeQueueRepository : QueueRepository {
    val dequeueCalls = mutableListOf<Pair<UUID, ConnectionResultEnum>>()

    override fun getServer(serverName: String): ServerQueueImpl = notImplemented()
    override fun getGroup(group: String): GroupQueueImpl = notImplemented()
    override fun getServerOrNull(serverName: String): ServerQueueImpl? = null
    override fun getGroupOrNull(group: String): GroupQueueImpl? = null
    override fun all(): Collection<BaseQueue<*>> = emptyList()
    override fun allServer(): Collection<ServerQueueImpl> = emptyList()
    override fun allGroup(): Collection<GroupQueueImpl> = emptyList()

    override suspend fun dequeueEverywhere(uuid: UUID, result: ConnectionResultEnum) {
        dequeueCalls += uuid to result
    }

    private fun notImplemented(): Nothing =
        throw UnsupportedOperationException("Not required for this test")
}