package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.standalone.server.queue.entry.PlayerQueueHandle
import dev.slne.surf.cloud.standalone.server.queue.entry.QueueEntryImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class QueueEntryImplTest {

    private fun newEntry(priority: Int, preferredServer: Long? = null) =
        QueueEntryImpl(
            PlayerQueueHandle(UUID.randomUUID()),
            priority,
            bypassFull = false,
            bypassQueue = false,
            preferredServerUid = preferredServer
        )

    @Test
    fun `compareTo honors priority`() {
        val high = newEntry(priority = 10)
        val low = newEntry(priority = 5)

        assertEquals(-1, high.compareTo(low))
        assertEquals(1, low.compareTo(high))
    }

    @Test
    fun `compareTo prioritizes preferred server`() {
        val a = newEntry(priority = 5, preferredServer = 1)
        val b = newEntry(priority = 5, preferredServer = null)

        assertEquals(-1, a.compareTo(b))
        assertEquals(1, b.compareTo(a))
    }

    @Test
    fun `compareTo equality when all fields equal`() {
        val a = newEntry(priority = 5, preferredServer = 1)
        val b = QueueEntryImpl(a.handle, 5, false, false, 1)

        assertEquals(0, a.compareTo(b))
        assertEquals(0, b.compareTo(a))
    }
}
