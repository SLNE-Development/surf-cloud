package dev.slne.surf.cloud.standalone.server.queue.entry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class QueueEntryImplTest {

    private fun newEntry(priority: Int, preferredServerName: String? = null) =
        QueueEntryImpl(
            PlayerQueueHandle(UUID.randomUUID()),
            priority,
            bypassFull = false,
            bypassQueue = false,
            preferredServerName = preferredServerName
        )

    @Test
    fun `compareTo honors priority`() {
        val high = newEntry(priority = 10)
        val low = newEntry(priority = 5)

        assertEquals(-1, high.compareTo(low), "Higher priority should come first")
        assertEquals(1, low.compareTo(high), "Lower priority should come after")
    }

    @Test
    fun `compareTo prioritizes preferred server`() {
        val a = newEntry(priority = 5, preferredServerName = "server1")
        val b = newEntry(priority = 5, preferredServerName = null)

        assertEquals(-1, a.compareTo(b))
        assertEquals(1, b.compareTo(a))
    }

    @Test
    fun `compareTo equality when all fields equal`() {
        val a = newEntry(priority = 5, preferredServerName = "server1")
        val b = a.copy()

        assertEquals(0, a.compareTo(b))
        assertEquals(0, b.compareTo(a))
    }
}