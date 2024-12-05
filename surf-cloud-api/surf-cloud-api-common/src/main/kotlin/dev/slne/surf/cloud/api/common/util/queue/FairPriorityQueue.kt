package dev.slne.surf.cloud.api.common.util.queue

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import java.util.*

/**
 * A priority queue that ensures fairness by using a FIFO ordering for elements with equal priority.
 *
 * This queue is backed by a [MutableList] and is not thread-safe.
 *
 * @param T The type of elements in the queue.
 * @property comparator The comparator used to order elements in the queue.
 */
class FairPriorityQueue<T>(private val comparator: Comparator<T>) : AbstractQueue<T>() {
    private val elements = mutableObjectListOf<Pair<T, Long>>()

    private val internalComparator = Comparator<Pair<T, Long>> { a, b ->
        val result = comparator.compare(a.first, b.first)
        if (result != 0) result else a.second.compareTo(b.second)
    }

    private val internalIterator = object : MutableIterator<T> {
        private val iterator = elements.iterator()

        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): T = iterator.next().first
        override fun remove() = iterator.remove()
    }

    @Volatile
    private var elementInsertionCounter = 0L

    override fun iterator(): MutableIterator<T> = internalIterator

    override val size get() = elements.size

    override fun offer(e: T?): Boolean {
        if (e == null) return false

        val element: Pair<T, Long> = e to elementInsertionCounter++
        val index = elements.binarySearch(element, internalComparator)
        elements.add(if (index < 0) -index - 1 else index, element)
        return true
    }

    override fun poll(): T? = elements.removeFirstOrNull()?.first

    override fun peek(): T? = elements.firstOrNull()?.first

    fun addFirst(e: T) {
        elements.addFirst(e to elementInsertionCounter++)
    }
}