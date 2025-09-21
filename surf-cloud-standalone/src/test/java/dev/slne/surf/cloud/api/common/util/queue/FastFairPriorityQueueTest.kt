package dev.slne.surf.cloud.api.common.util.queue

import org.junit.jupiter.api.Assertions.*
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

    @Test
    fun `offer null is ignored`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())

        assertFalse(queue.offer(null))
        assertEquals(0, queue.size)
        assertNull(queue.peek())
    }

    @Test
    fun `poll on empty queue returns null`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())

        assertNull(queue.poll())
        assertEquals(0, queue.size)
    }

    @Test
    fun `peek does not remove element`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(4)
        queue.offer(2)

        val peeked = queue.peek()

        assertEquals(2, peeked)
        assertEquals(2, queue.size)
        assertEquals(2, queue.poll())
    }

    @Test
    fun `iterator provides snapshot view`() {
        val queue = FastFairPriorityQueue<Int>(Comparator.naturalOrder())
        queue.offer(5)
        queue.offer(1)

        val iterator = queue.iterator()

        queue.offer(3)

        val iterated = mutableListOf<Int>()
        while (iterator.hasNext()) {
            iterated += iterator.next()
        }

        assertEquals(listOf(1, 5), iterated)
        assertEquals(listOf(1, 3, 5), queue.snapshot().toList())
    }
}