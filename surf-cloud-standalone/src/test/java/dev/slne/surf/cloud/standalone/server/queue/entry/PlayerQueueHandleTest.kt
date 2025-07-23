package dev.slne.surf.cloud.standalone.server.queue.entry

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class PlayerQueueHandleTest {

    @Test
    fun `complete delivers result`() = runBlocking {
        val handle = PlayerQueueHandle(UUID.randomUUID())
        handle.complete(ConnectionResultEnum.SUCCESS)
        assertEquals(ConnectionResultEnum.SUCCESS, handle.result.await())
    }

    @Test
    fun `cancel completes with cancelled`() = runBlocking {
        val handle = PlayerQueueHandle(UUID.randomUUID())
        handle.cancel()
        assertEquals(ConnectionResultEnum.CONNECTION_CANCELLED, handle.result.await())
    }
}