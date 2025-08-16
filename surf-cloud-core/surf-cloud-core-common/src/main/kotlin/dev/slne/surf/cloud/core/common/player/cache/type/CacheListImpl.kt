package dev.slne.surf.cloud.core.common.player.cache.type

import dev.slne.surf.cloud.api.common.netty.network.codec.encodeToByteArrayDynamic
import dev.slne.surf.cloud.api.common.player.cache.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.CacheList
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheListDeltaPacket.ListOp
import dev.slne.surf.cloud.core.common.player.cache.CloudPlayerCacheImpl
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class CacheListImpl<E : Any>(
    private val owner: CloudPlayerCacheImpl,
    private val key: CacheKey.List<E>,
) : CacheList<E> {
    private val lock = ReentrantReadWriteLock()
    private val list = ObjectArrayList<E>()

    override var suppressOutbound: Boolean = false

    override fun subList(from: Int, to: Int) = snapshot().subList(from, to)

    override fun size(size: Int) {
        throw UnsupportedOperationException("CacheList size cannot be set directly")
    }

    override fun getElements(
        from: Int,
        a: Array<out Any?>?,
        offset: Int,
        length: Int
    ) {
        lock.read {
            list.getElements(from, a, offset, length)
        }
    }

    override fun removeElements(from: Int, to: Int) {
        throw UnsupportedOperationException("CacheList elements cannot be removed directly")
    }

    override fun addElements(index: Int, a: Array<out E?>?) {
        list.addElements(index, a)
    }

    override fun addElements(
        index: Int,
        a: Array<out E?>?,
        offset: Int,
        length: Int
    ) {
        list.addElements(index, a, offset, length)
    }

    override val size: Int
        get() = lock.read { list.size }

    override fun contains(element: E?) = lock.read { list.contains(element) }
    override fun containsAll(elements: Collection<E?>) = lock.read { list.containsAll(elements) }
    override fun get(index: Int): E = lock.read { list[index] }
    override fun indexOf(element: E?) = lock.read { list.indexOf(element) }
    override fun isEmpty() = lock.read { list.isEmpty() }
    override fun lastIndexOf(element: E?) = lock.read { list.lastIndexOf(element) }

    override fun add(element: E): Boolean {
        lock.write { list.add(element) }
        sendOps(ListOp.Add(null, key.elementCodec.encodeToByteArrayDynamic(element)))
        return true
    }

    override fun add(index: Int, element: E) {
        lock.write { list.add(index, element) }
        sendOps(ListOp.Add(index, key.elementCodec.encodeToByteArrayDynamic(element)))
    }

    override fun addAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        val ok = lock.write { list.addAll(elements) }
        if (ok) {
            val ops = elements.mapIndexed { i, e ->
                ListOp.Add(
                    null,
                    key.elementCodec.encodeToByteArrayDynamic(e)
                )
            }
            sendOps(*ops.toTypedArray())
        }
        return ok
    }

    override fun addAll(
        index: Int,
        elements: Collection<E>
    ): Boolean {
        if (elements.isEmpty()) return false
        var i = index
        val ok = lock.write { list.addAll(index, elements) }
        if (ok) {
            val ops =
                elements.map { e -> ListOp.Add(i++, key.elementCodec.encodeToByteArrayDynamic(e)) }
            sendOps(*ops.toTypedArray())
        }
        return ok
    }

    override fun clear() {
        lock.write { list.clear() }
        sendOps(ListOp.Clear)
    }

    override fun remove(element: E?): Boolean {
        var idx = -1
        lock.write {
            idx = list.indexOf(element)
            if (idx >= 0) list.removeAt(idx)
        }
        if (idx >= 0) {
            sendOps(ListOp.RemoveAt(idx))
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        var changed = false
        val ops = mutableListOf<ListOp>()
        lock.write {
            for (element in elements) {
                val idx = list.indexOf(element)
                if (idx >= 0) {
                    list.removeAt(idx)
                    ops.add(ListOp.RemoveAt(idx))
                    changed = true
                }
            }
        }
        if (changed) {
            sendOps(*ops.toTypedArray())
        }
        return changed
    }

    override fun removeAt(index: Int): E? {
        var removed: E? = null
        lock.write {
            removed = list.removeAt(index)
            sendOps(ListOp.RemoveAt(index))
        }
        return removed
    }

    override fun retainAll(elements: Collection<E?>): Boolean {
        if (elements.isEmpty()) return false
        var changed = false
        val ops = mutableListOf<ListOp>()
        lock.write {
            for (i in list.indices.reversed()) {
                if (!elements.contains(list[i])) {
                    list.removeAt(i)
                    ops.add(ListOp.RemoveAt(i))
                    changed = true
                }
            }
        }
        if (changed) {
            sendOps(*ops.toTypedArray())
        }
        return changed
    }

    override fun set(index: Int, element: E): E? {
        var oldElement: E? = null
        lock.write {
            oldElement = list.set(index, element)
            sendOps(ListOp.Set(index, key.elementCodec.encodeToByteArrayDynamic(element)))
        }
        return oldElement
    }

    override fun compareTo(other: List<E?>?): Int {
        return snapshot().compareTo(other)
    }

    override fun iterator() = snapshot().iterator()
    override fun listIterator() = snapshot().listIterator()
    override fun listIterator(index: Int) = snapshot().listIterator(index)
    override fun snapshot() = lock.read { ObjectArrayList(list) }

    private fun sendOps(vararg ops: ListOp) {
        if (suppressOutbound) return
        owner.sendListDelta(key, ops)
    }
}
