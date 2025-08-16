package dev.slne.surf.cloud.core.common.player.cache

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.encodeToByteArrayDynamic
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.cache.*
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheSetDeltaPacket.SetOp
import dev.slne.surf.cloud.core.common.player.cache.entry.EntryMeta
import dev.slne.surf.cloud.core.common.player.cache.type.CacheListImpl
import dev.slne.surf.cloud.core.common.player.cache.type.CacheMapImpl
import dev.slne.surf.cloud.core.common.player.cache.type.CacheSetImpl
import dev.slne.surf.cloud.core.common.player.cache.type.CacheStructuredImpl
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class CloudPlayerCacheImpl(
    override val uuid: UUID,
    private val wire: CacheWire,
    private val changeCounter: ChangeCounter
) : CloudPlayerCache {
    private val data = ConcurrentHashMap<CacheKey<*>, Any>()
    private val meta = ConcurrentHashMap<CacheKey<*>, EntryMeta>()

    private var batching = false
    private val batchPackets = mutableObjectListOf<NettyPacket>()

    fun <T : Any> encode(codec: StreamCodec<SurfByteBuf, T>, value: T): ByteArray =
        codec.encodeToByteArrayDynamic(value)

    fun <T : Any> decode(typeId: String, bytes: ByteArray): T {
        val ser = CacheTypeRegistry.resolveOrThrow<T>(typeId)
        return cacheJson.decodeFromString(ser, bytes.decodeToString())
    }


    private fun send(packet: NettyPacket) {
        if (batching) batchPackets += packet
        else wireSend(packet)
    }

    private fun wireSend(packet: NettyPacket) {
        wire.sendToServer(packet)
    }

    protected open fun sendToClientsDirect(packet: NettyPacket) {
    }

    override fun <T : Any> get(key: CacheKey.Value<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return data[key] as? T
    }

    override fun <T : Any> set(
        key: CacheKey.Value<T>,
        value: T
    ) {
        data[key] = value
        meta[key] = EntryMeta.Value(key, changeCounter.next())
        val payload = encode(key.codec, value)
        send(
            PlayerCacheValueUpdatePacket(
                uuid,
                key,
                payload,
                changeCounter.current()
            )
        )
    }

    override fun remove(key: CacheKey<*>) {
        data.remove(key)
        meta[key] = EntryMeta.Removed(key, changeCounter.next())
        send(
            PlayerCacheRemoveKeyPacket(
                uuid,
                key,
                changeCounter.current()
            )
        )
    }

    override fun <E : Any> list(key: CacheKey.List<E>): CacheList<E> {
        @Suppress("UNCHECKED_CAST")
        return data.computeIfAbsent(key) {
            CacheListImpl(this, key).also {
                meta[key] = EntryMeta.List(key, changeCounter.current())
            }
        } as CacheList<E>
    }

    override fun <E : Any> set(key: CacheKey.Set<E>): CacheSet<E> {
        @Suppress("UNCHECKED_CAST")
        return data.computeIfAbsent(key) {
            CacheSetImpl(this, key).also {
                meta[key] = EntryMeta.Set(key, changeCounter.current())
            }
        } as CacheSet<E>
    }

    override fun <K : Any, V : Any> map(key: CacheKey.Map<K, V>): CacheMap<K, V> {
        @Suppress("UNCHECKED_CAST")
        return data.computeIfAbsent(key) {
            CacheMapImpl(this, key).also {
                meta[key] = EntryMeta.Map(key, changeCounter.current())
            }
        } as CacheMap<K, V>
    }

    override fun <T : Any, D : Any> structured(
        key: CacheKey.Structured<T, D>,
        type: StructuredType<T, D>,
        default: () -> T
    ): CacheStructured<T, D> {
        @Suppress("UNCHECKED_CAST")
        return data.computeIfAbsent(key) {
            CacheStructuredImpl(this, key, default(), type).also {
                meta[key] = EntryMeta.Structured(key, changeCounter.current())
            }
        } as CacheStructured<T, D>
    }


    override fun <R> batch(block: CloudPlayerCacheBatch.() -> R): R {
        val before = batching
        batching = true

        return try {
            DefaultBatch(this).block()
        } finally {
            batching = before
            val packets = ObjectArrayList(batchPackets)
            batchPackets.clear()
            wire.sendToServer(ServerboundBundlePacket(packets))
        }
    }

    override fun clear() {
        data.clear()
        meta.clear()
    }

    fun <E : Any> sendListDelta(
        key: CacheKey.List<E>,
        ops: Array<out PlayerCacheListDeltaPacket.ListOp>
    ) {
        meta[key] as? EntryMeta.List<E> ?: run {
            meta[key] = EntryMeta.List(key, changeCounter.next())
            null
        }
        val packet = PlayerCacheListDeltaPacket(
            uuid,
            key,
            ops = ops,
            changeId = changeCounter.next()
        )
        send(packet)
    }

    fun <E : Any> sendSetDelta(
        key: CacheKey.Set<E>,
        ops: Array<out SetOp>
    ) {
        meta[key] as? EntryMeta.Set<E> ?: run {
            meta[key] = EntryMeta.Set(key, changeCounter.next())
            null
        }
        val packet = PlayerCacheSetDeltaPacket(
            uuid,
            key,
            ops = ops,
            changeId = changeCounter.next()
        )
        send(packet)
    }

    fun <K : Any, V : Any> sendMapDelta(
        key: CacheKey.Map<K, V>,
        ops: Array<out PlayerCacheMapDeltaPacket.MapOp>
    ) {
        meta[key] as? EntryMeta.Map<K, V> ?: run {
            meta[key] = EntryMeta.Map(key, changeCounter.next())
            null
        }
        val packet = PlayerCacheMapDeltaPacket(
            uuid,
            key,
            ops = ops,
            changeId = changeCounter.next()
        )
        send(packet)
    }


    fun <T : Any, D : Any> sendStructuredDelta(
        key: CacheKey.Structured<T, D>,
        type: StructuredType<T, D>,
        delta: D
    ) {
        val payload = encode(type.deltaCodec, delta)
        val packet = PlayerCacheValueUpdatePacket(
            uuid,
            key,
            payload = payload,
            changeId = changeCounter.next()
        )
        send(packet)
    }


    private class DefaultBatch(private val cache: CloudPlayerCacheImpl) : CloudPlayerCacheBatch {
        override fun <T : Any> set(
            key: CacheKey.Value<T>,
            value: T
        ) {
            cache[key] = value
        }

        override fun remove(key: CacheKey<*>) {
            cache.remove(key)
        }

        override fun <E : Any> listAdd(
            key: CacheKey.List<E>,
            element: E,
            index: Int?
        ) {
            val list = cache.list(key)
            if (index == null) list.add(element) else list.add(index, element)
        }

        override fun <E : Any> listRemoveAt(
            key: CacheKey.List<E>,
            index: Int
        ) {
            cache.list(key).removeAt(index)
        }

        override fun <E : Any> setAdd(
            key: CacheKey.Set<E>,
            element: E
        ) {
            cache.set(key).add(element)
        }

        override fun <E : Any> setRemove(
            key: CacheKey.Set<E>,
            element: E
        ) {
            cache.set(key).remove(element)
        }

        override fun <K : Any, V : Any> mapPut(
            key: CacheKey.Map<K, V>,
            k: K,
            v: V
        ) {
            cache.map(key).put(k, v)
        }

        override fun <K : Any, V : Any> mapRemove(
            key: CacheKey.Map<K, V>,
            k: K
        ) {
            cache.map(key).remove(k)
        }

    }
}