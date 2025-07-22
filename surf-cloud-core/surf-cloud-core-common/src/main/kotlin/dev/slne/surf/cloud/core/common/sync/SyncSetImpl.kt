package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.sync.SyncSet
import dev.slne.surf.cloud.api.common.sync.SyncSetListener
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.toObjectSet
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

class SyncSetImpl<T>(override val id: String, val valueCodec: StreamCodec<SurfByteBuf, T>) :
    SyncSet<T> {
    override val codec: StreamCodec<SurfByteBuf, Set<T>> = StreamCodec.of(
        { buf, value ->
            buf.writeCollection(value) { buf, item -> valueCodec.encode(buf, item) }
        },
        { buf ->
            buf.readCollection({ mutableObjectSetOf(it) }) { buf -> valueCodec.decode(buf) }
        }
    )

    private val backing = ConcurrentHashMap.newKeySet<T>()
    private val listeners = CopyOnWriteArrayList<SyncSetListener<T>>()
    private val changeCounter = AtomicLong()


    init {
        CommonSyncRegistryImpl.instance.register(this)
    }

    override fun subscribe(listener: SyncSetListener<T>) = listeners.addIfAbsent(listener)

    fun addInternal(element: T) = backing.add(element)
    fun removeInternal(element: T) = backing.remove(element)
    fun addAllInternal(elements: Collection<T>) = backing.addAll(elements)

    override fun add(element: T) = backing.add(element).also { added ->
        if (added) fireDelta(true, element)
    }

    override fun remove(element: T) = backing.remove(element).also { removed ->
        if (removed) fireDelta(false, element)
    }

    private fun fireDelta(added: Boolean, element: T) {
        val changeId = changeCounter.incrementAndGet()
        callListeners(added, element)
        CommonSyncRegistryImpl.instance.afterChange(this, added, changeId, element)
    }

    private fun callListeners(added: Boolean, element: T) {
        for (listener in listeners) {
            try {
                listener(added, element)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Error while notifying listener for SyncSet '$id'")
            }
        }
    }

    override val size: Int get() = backing.size
    override fun contains(element: T) = backing.contains(element)
    override fun containsAll(elements: Collection<T>) = backing.containsAll(elements)
    override fun isEmpty() = backing.isEmpty()
    override fun addAll(elements: Collection<T>) = elements.map(::add).any { it }
    override fun retainAll(elements: Collection<T>) =
        backing.filterNot { it in elements }.any { remove(it) }


    override fun removeAll(elements: Collection<T>) = elements.map(::remove).any { it }

    override fun clear() = backing.toList().forEach { remove(it) }
    override fun iterator(): ObjectIterator<T> = SyncSetIterator()
    override fun snapshot(): ObjectSet<T> = backing.toObjectSet()

    private inner class SyncSetIterator : ObjectIterator<T> {
        private val iterator = backing.iterator()
        private var last: T? = null

        override fun hasNext() = iterator.hasNext()

        override fun next(): T {
            val nextElement = iterator.next()
            last = nextElement
            return nextElement
        }

        override fun remove() {
            val elementToRemove = last ?: error("next() must be called before remove()")
            this@SyncSetImpl.remove(elementToRemove)
            last = null
        }

        override fun forEachRemaining(action: Consumer<in T>) {
            iterator.forEachRemaining(action)
        }
    }

    companion object {
        private val log = logger()
    }
}