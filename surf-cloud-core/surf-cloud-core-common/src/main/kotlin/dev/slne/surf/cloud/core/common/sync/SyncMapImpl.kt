package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.sync.SyncMap
import dev.slne.surf.cloud.api.common.sync.SyncMapListener
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObject2ObjectMap
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.ObjectIterator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiConsumer
import java.util.function.Consumer

class SyncMapImpl<K, V>(
    override val id: String,
    val keyCodec: StreamCodec<SurfByteBuf, K>,
    val valueCodec: StreamCodec<SurfByteBuf, V>
) : SyncMap<K, V> {
    override val codec: StreamCodec<SurfByteBuf, Map<K, V>> = StreamCodec.of(
        { buf, value ->
            buf.writeMap(value,
                { buf, key -> keyCodec.encode(buf, key) },
                { buf, v -> valueCodec.encode(buf, v) }
            )
        },
        { buf ->
            buf.readMap(
                { mutableObject2ObjectMapOf(it) },
                { buf -> keyCodec.decode(buf) },
                { buf -> valueCodec.decode(buf) }
            )
        }
    )

    private val backing = ConcurrentHashMap<K, V>()
    private val listeners = CopyOnWriteArrayList<SyncMapListener<K, V>>()
    private val changeCounter = AtomicLong()

    init {
        CommonSyncRegistryImpl.instance.register(this)
    }

    override fun subscribe(listener: SyncMapListener<K, V>) = listeners.addIfAbsent(listener)

    fun putInternal(key: K, value: V): V? = backing.put(key, value)
    fun removeInternal(key: K): V? = backing.remove(key)
    fun putAllInternal(entries: Map<K, V>) = backing.putAll(entries)

    override fun put(key: K, value: V): V? {
        val oldValue = backing.put(key, value)
        if (oldValue != value) {
            fireDelta(key, oldValue, value)
        }
        return oldValue
    }

    override fun remove(key: K): V? {
        val oldValue = backing.remove(key)
        if (oldValue != null) {
            fireDelta(key, oldValue, null)
        }
        return oldValue
    }

    private fun fireDelta(key: K, oldValue: V?, newValue: V?) {
        val changeId = changeCounter.incrementAndGet()
        callListeners(key, oldValue, newValue)
        CommonSyncRegistryImpl.instance.afterChange(this, key, oldValue, newValue, changeId)
    }

    private fun callListeners(key: K, oldValue: V?, newValue: V?) {
        for (listener in listeners) {
            try {
                listener(key, oldValue, newValue)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Error while notifying listener for SyncMap '$id'")
            }
        }
    }

    override val size: Int get() = backing.size
    override fun containsKey(key: K) = backing.containsKey(key)
    override fun containsValue(value: V) = backing.containsValue(value)
    override fun get(key: K): V? = backing[key]
    override fun isEmpty() = backing.isEmpty()

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (key, value) -> put(key, value) }
    }

    override fun clear() {
        backing.keys.toList().forEach { remove(it) }
    }

    override fun iterator(): ObjectIterator<Object2ObjectMap.Entry<K, V>> = SyncMapIterator()

    override fun snapshot(): Object2ObjectMap<K, V> = backing.toObject2ObjectMap()

    override val keys: MutableSet<K> get() = backing.keys
    override val values: MutableCollection<V> get() = backing.values
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = backing.entries

    override fun object2ObjectEntrySet(): Object2ObjectMap.FastEntrySet<K, V> {
        return object : Object2ObjectMap.FastEntrySet<K, V> {
            override val size: Int get() = backing.size
            override fun isEmpty() = backing.isEmpty()
            override fun contains(element: Object2ObjectMap.Entry<K, V>?) = 
                element != null && backing[element.key] == element.value
            override fun iterator(): ObjectIterator<Object2ObjectMap.Entry<K, V>> = 
                this@SyncMapImpl.iterator()
            override fun add(element: Object2ObjectMap.Entry<K, V>): Boolean {
                put(element.key, element.value)
                return true
            }
            override fun remove(element: Object2ObjectMap.Entry<K, V>?): Boolean {
                if (element != null && backing[element.key] == element.value) {
                    this@SyncMapImpl.remove(element.key)
                    return true
                }
                return false
            }
            override fun containsAll(elements: Collection<Object2ObjectMap.Entry<K, V>>) = 
                elements.all { contains(it) }
            override fun addAll(elements: Collection<Object2ObjectMap.Entry<K, V>>): Boolean {
                var modified = false
                for (entry in elements) {
                    put(entry.key, entry.value)
                    modified = true
                }
                return modified
            }
            override fun removeAll(elements: Collection<Object2ObjectMap.Entry<K, V>>): Boolean {
                var modified = false
                for (entry in elements) {
                    if (remove(entry)) modified = true
                }
                return modified
            }
            override fun retainAll(elements: Collection<Object2ObjectMap.Entry<K, V>>): Boolean {
                val toRemove = backing.entries.filter { entry ->
                    !elements.any { it.key == entry.key && it.value == entry.value }
                }
                var modified = false
                for (entry in toRemove) {
                    this@SyncMapImpl.remove(entry.key)
                    modified = true
                }
                return modified
            }
            override fun clear() = this@SyncMapImpl.clear()
            override fun fastIterator(): ObjectIterator<Object2ObjectMap.Entry<K, V>> = iterator()
            override fun fastForEach(consumer: Consumer<in Object2ObjectMap.Entry<K, V>>) {
                backing.forEach { (k, v) ->
                    consumer.accept(object : Object2ObjectMap.Entry<K, V> {
                        override val key = k
                        override val value = v
                        override fun setValue(value: V): V {
                            val old = backing[k]
                            put(k, value)
                            return old ?: value
                        }
                    })
                }
            }
        }
    }

    private inner class SyncMapIterator : ObjectIterator<Object2ObjectMap.Entry<K, V>> {
        private val iterator = backing.entries.iterator()
        private var last: K? = null

        override fun hasNext() = iterator.hasNext()

        override fun next(): Object2ObjectMap.Entry<K, V> {
            val entry = iterator.next()
            last = entry.key
            return object : Object2ObjectMap.Entry<K, V> {
                override val key = entry.key
                override val value = entry.value
                override fun setValue(value: V): V {
                    val old = entry.value
                    put(entry.key, value)
                    return old
                }
            }
        }

        override fun remove() {
            val keyToRemove = last ?: error("next() must be called before remove()")
            this@SyncMapImpl.remove(keyToRemove)
            last = null
        }

        override fun forEachRemaining(action: Consumer<in Object2ObjectMap.Entry<K, V>>) {
            iterator.forEachRemaining { entry ->
                action.accept(object : Object2ObjectMap.Entry<K, V> {
                    override val key = entry.key
                    override val value = entry.value
                    override fun setValue(value: V): V {
                        val old = entry.value
                        put(entry.key, value)
                        return old
                    }
                })
            }
        }
    }

    companion object {
        private val log = logger()
    }
}
