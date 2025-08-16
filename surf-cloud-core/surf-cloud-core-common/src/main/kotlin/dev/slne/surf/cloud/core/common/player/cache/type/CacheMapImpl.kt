package dev.slne.surf.cloud.core.common.player.cache.type

import dev.slne.surf.cloud.api.common.netty.network.codec.encodeToByteArrayDynamic
import dev.slne.surf.cloud.api.common.player.cache.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.CacheMap
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheMapDeltaPacket.*
import dev.slne.surf.cloud.core.common.player.cache.CloudPlayerCacheImpl
import org.gradle.internal.impldep.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import java.util.concurrent.ConcurrentHashMap

class CacheMapImpl<K : Any, V : Any>(
    private val owner: CloudPlayerCacheImpl,
    private val key: CacheKey.Map<K, V>,
) : ConcurrentHashMap<K, V>(), CacheMap<K, V> {
    override var suppressOutbound: Boolean = false

    override fun put(key: K, value: V): V? {
        val prev = super.put(key, value)
        sendOps(
            MapOp.Put(
                this.key.keyCodec.encodeToByteArrayDynamic(key),
                this.key.valueCodec.encodeToByteArrayDynamic(value)
            )
        )
        return prev
    }

    override fun putAll(from: Map<out K, V>) {
        super.putAll(from)
        sendOps(*from.map { (k, v) ->
            MapOp.Put(
                this.key.keyCodec.encodeToByteArrayDynamic(k),
                this.key.valueCodec.encodeToByteArrayDynamic(v)
            )
        }.toTypedArray())
    }

    override fun remove(key: K): V? {
        val prev = super.remove(key)
        if (prev != null) {
            sendOps(MapOp.Remove(this.key.keyCodec.encodeToByteArrayDynamic(key)))
        }
        return prev
    }

    override fun clear() {
        super.clear()
        sendOps(MapOp.Clear)
    }

    override fun snapshot() = Object2ObjectOpenHashMap(this)


    private fun sendOps(vararg ops: MapOp) {
        if (suppressOutbound) return
        owner.sendMapDelta(key as CacheKey<Any>, keySerializer, valueSerializer, ops)
    }
}