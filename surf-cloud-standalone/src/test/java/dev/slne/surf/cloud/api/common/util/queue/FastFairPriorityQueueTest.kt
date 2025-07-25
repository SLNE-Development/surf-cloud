package dev.slne.surf.cloud.api.common.util.queue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FastFairPriorityQueueTest {

    @Test
    fun `fifo order for equal priority`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(1)
        queue.offer(1)
        queue.offer(1)

        val first = queue.poll()
        val second = queue.poll()
        val third = queue.poll()

        assertEquals(listOf(1, 1, 1), listOf(first, second, third))
    }

    @Test
    fun `addFirst inserts ahead of others`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(1)
        queue.addFirst(1)
        queue.offer(2)

        assertEquals(1, queue.poll())
        assertEquals(1, queue.poll())
        assertEquals(2, queue.poll())
    }

    @Test
    fun `snapshot returns ordered list`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(3)
        queue.offer(1)
        queue.offer(2)

        assertEquals(listOf(1, 2, 3), queue.snapshot().toList())
    }

}