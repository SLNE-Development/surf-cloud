package dev.slne.surf.cloud.api.common.util.queue

interface SuspendingQueue<E> : MutableIterable<E> {

    suspend fun offer(e: E): Boolean

    fun remove(): E
    fun remove(element: E): Boolean
    fun poll(): E?

    fun peek(): E?
    fun element(): E

    fun clear()
    suspend fun offerAll(c: Collection<E>): Boolean

    fun contains(element: E): Boolean
    fun isEmpty(): Boolean
}