package dev.slne.surf.cloud.api.common.util.queue

import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import it.unimi.dsi.fastutil.objects.*
import java.io.Serial
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

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

    private val internalIterator = object : ObjectListIterator<T> {
        private val iterator = elements.iterator()

        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): T = iterator.next().first
        override fun nextIndex(): Int = iterator.nextIndex()
        override fun previousIndex(): Int = iterator.previousIndex()
        override fun remove() = iterator.remove()
        override fun previous(): T? = iterator.previous()?.first
        override fun hasPrevious(): Boolean = iterator.hasPrevious()
        override fun back(n: Int): Int = iterator.back(n)
        override fun skip(n: Int): Int = iterator.skip(n)
        override fun forEachRemaining(action: Consumer<in T>) =
            iterator.forEachRemaining { action.accept(it.first) }

        override fun set(k: T?) {
            if (k == null) {
                iterator.set(null)
            } else {
                iterator.set(k to elementInsertionCounter++)
            }
        }

        override fun add(k: T?) {
            if (k == null) {
                iterator.add(null)
            } else {
                iterator.add(k to elementInsertionCounter++)
            }
        }
    }

    @Volatile
    private var elementInsertionCounter = 0L

    override fun iterator(): ObjectListIterator<T> = internalIterator

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

    fun snapshot(): ObjectList<T> {
        return elements.mapTo(mutableObjectListOf()) { it.first }
    }
}

/**
 * A **stable** priority queue backed by a binary heap from *fastutil*.
 *
 * *   **Fair ordering** – ties (equal priority) are resolved by insertion order
 *     thanks to a monotonically increasing sequence number.
 * *   **Time-complexity** – `offer`, `poll`, `peek` are **O(log n)**,
 *     while iteration and snapshots are **O(n log n)** (once) + **O(1)**
 *     per element.
 * *   **Memory-efficient** – only one extra `long` per element; no boxing.
 *
 * @param T       the element type held by this queue
 * @param prioCmp comparator that determines the *primary* priority.
 *               When it returns `0`, FIFO order is applied.
 */
class FastFairPriorityQueue<T>(private val prioCmp: Comparator<in T>) : AbstractQueue<T>() {

    /**
     * Internal wrapper that stores the user element together with its
     * insertion sequence number in order to guarantee FIFO tie-breaking.
     */
    private data class Entry<E>(val value: E, val seq: Long)

    /** Monotonically increasing timestamp used for FIFO. */
    private val seq = AtomicLong()

    /** Comparator that first applies user priority, then FIFO tie-break. */
    private val entryCmp = Comparator<Entry<T>> { a, b ->
        val r = prioCmp.compare(a.value, b.value)
        if (r != 0) r else a.seq.compareTo(b.seq)
    }

    /** Heap provided by fastutil with protected access to the backing array. */
    private val heap = ExposedHeap<Entry<T>>(entryCmp)

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e element to be added, ignored when `null`
     * @return `true` if the element was added, `false` when `e == null`
     */
    override fun offer(e: T?): Boolean {
        if (e == null) return false
        heap.enqueue(Entry(e, seq.getAndIncrement()))
        return true
    }

    /**
     * Retrieves and removes the head of this queue, or returns `null`
     * if this queue is empty.
     */
    override fun poll(): T? = if (heap.isEmpty) null else heap.dequeue().value

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns `null` if this queue is empty.
     */
    override fun peek(): T? = if (heap.isEmpty) null else heap.first().value

    /** Current number of elements in the queue (O(1)). */
    override val size: Int get() = heap.size()

    /**
     * Inserts an element ahead of every other entry – useful for urgent
     * items. Internally it assigns a sequence smaller than any regular
     * entry so the FIFO invariant still holds.
     *
     * @param e element to insert at the logical front of the queue
     */
    fun addFirst(e: T) = heap.enqueue(Entry(e, Long.MIN_VALUE + seq.getAndIncrement()))

    /**
     * Returns an immutable **snapshot** of the queue contents in *priority
     * order*. The original queue remains untouched.
     *
     * @return a newly–allocated [ObjectArrayList] containing the elements
     *         sorted first by `prioCmp`, then by FIFO insertion order
     *
     * @implNote Runs in **O(n log n)** time and **O(n)** extra memory by copying the
     * underlying heap array and applying `ObjectArrays.stableSort`.
     */
    fun snapshot(): ObjectArrayList<T> {
        val size = heap.size()
        if (size == 0) return ObjectArrayList()

        val raw = heap.backing()
        val buf: Array<Entry<T>?> = arrayOfNulls(size)
        System.arraycopy(raw, 0, buf, 0, size)
        ObjectArrays.stableSort(buf, entryCmp)

        val result = ObjectArrayList<T>(size)
        for (e in buf) e?.let { result.add(it.value) }
        return result
    }

    /**
     * Returns an iterator over a **stable snapshot** of the queue.
     * The iterator is *fail-safe* – structural modifications to the queue
     * after creation do **not** affect the iteration.
     *
     * Each call to `iterator()` allocates the snapshot once (O(n log n)),
     * then returns an [ObjectListIterator] with **O(1)** per `next()`.
     */
    override fun iterator() = snapshot().iterator()

    /**
     * Small helper subclass that exposes the protected `heap` array so we
     * can copy it without reflective access.
     */
    private class ExposedHeap<E>(
        cmp: Comparator<E>
    ) : ObjectHeapPriorityQueue<E>(cmp) {
        fun backing(): Array<Any?> = heap as Array<Any?>

        companion object {
            @Serial
            @JvmStatic
            private val serialVersionUID: Long = 7675782955405448755L
        }
    }

}

class FairSuspendPriorityQueue<T>(private val comparator: SuspendComparator<T>) :
    AbstractSuspendingQueue<T>() {
    private val elements = mutableObjectListOf<Pair<T, Long>>()

    private val internalComparator: SuspendComparator<Pair<T, Long>> = { a, b ->
        val result = comparator(a.first, b.first)
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

    override suspend fun offer(e: T): Boolean {
        if (e == null) return false

        val element: Pair<T, Long> = e to elementInsertionCounter++
        val index = findInsertIndex(element)
        elements.add(if (index < 0) -index - 1 else index, element)
        return true
    }

    override fun poll(): T? = elements.removeFirstOrNull()?.first

    override fun peek(): T? = elements.firstOrNull()?.first

    fun addFirst(e: T) {
        elements.addFirst(e to elementInsertionCounter++)
    }

    /**
     * Finds the index where the element should be inserted using the suspendable comparator.
     */
    private suspend fun findInsertIndex(element: Pair<T, Long>): Int {
        var low = 0
        var high = elements.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val comparison = internalComparator(element, elements[mid])
            when {
                comparison < 0 -> high = mid - 1
                comparison > 0 -> low = mid + 1
                else -> return mid
            }
        }

        return low
    }
}

typealias SuspendComparator<T> = suspend (T, T) -> Int