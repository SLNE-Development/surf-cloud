package dev.slne.surf.cloud.api.common.util.queue

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FairPriorityQueueTest {
    @Test
    fun `fifo order is preserved when priorities match`() {
        val queue = FairPriorityQueue<Int> { _, _ -> 0 }
        queue.offer(1)
        queue.offer(2)
        queue.offer(3)

        val order = listOf(queue.poll(), queue.poll(), queue.poll())

        assertEquals(listOf(1, 2, 3), order)
        assertNull(queue.poll())
    }


    @Test
    fun `addFirst inserts element before others`() {
        val queue = FairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(5)
        queue.offer(10)

        queue.addFirst(1)

        assertEquals(listOf(1, 5, 10), queue.snapshot().toList())
        assertEquals(3, queue.size)
    }

    @Test
    fun `offer null does not modify queue`() {
        val queue = FairPriorityQueue<Int>(Comparator.naturalOrder())

        assertFalse(queue.offer(null))
        assertEquals(0, queue.size)
        assertNull(queue.peek())
    }

    @Test
    fun `iterator removal updates backing storage`() {
        val queue = FairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(3)
        queue.offer(1)
        queue.offer(2)

        val iterator = queue.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.next())

        iterator.remove()

        assertEquals(listOf(2, 3), queue.snapshot().toList())
        assertEquals(2, queue.size)
    }
}