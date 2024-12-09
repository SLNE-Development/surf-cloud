package dev.slne.surf.cloud.api.common.util.queue

abstract class AbstractSuspendingQueue<E> protected constructor() : SuspendingQueue<E> {
    abstract val size: Int

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun contains(element: E): Boolean {
        val it = iterator()
        if (element == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return true
                }
            }
        } else {
            while (it.hasNext()) {
                if (element == it.next()) {
                    return true
                }
            }
        }
        return false
    }

    override fun remove(): E {
        return poll() ?: throw NoSuchElementException()
    }

    override fun remove(element: E): Boolean {
        val it = iterator()
        if (element == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove()
                    return true
                }
            }
        } else {
            while (it.hasNext()) {
                if (element == it.next()) {
                    it.remove()
                    return true
                }
            }
        }
        return false
    }

    override fun element(): E {
        return peek() ?: throw NoSuchElementException()
    }

    override fun clear() {
        while (poll() != null);
    }

    override fun addAll(c: Collection<E>): Boolean {
        if (c === this) {
            throw IllegalArgumentException()
        }
        var modified = false
        for (e in c) {
            if (add(e)) {
                modified = true
            }
        }

        return modified
    }
}