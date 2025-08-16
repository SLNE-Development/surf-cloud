package dev.slne.surf.cloud.core.common.player.cache.type

import dev.slne.surf.cloud.api.common.player.cache.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.CacheStructured
import dev.slne.surf.cloud.api.common.player.cache.StructuredType
import dev.slne.surf.cloud.core.common.player.cache.CloudPlayerCacheImpl
import java.util.concurrent.atomic.AtomicBoolean

class CacheStructuredImpl<T : Any, D : Any>(
    private val owner: CloudPlayerCacheImpl,
    private val key: CacheKey.Structured<T, D>,
    override var value: T,
    override val type: StructuredType<T, D>
) : CacheStructured<T, D> {
    private val dirty = AtomicBoolean(false)

    override fun markDirty() {
        dirty.set(true)
    }

    override fun consumeDirty(): Boolean = dirty.getAndSet(false)

    override fun emit(delta: D) {
        value = type.applyDelta(value, delta)
        owner.sendStructuredDelta(key, type, delta)
        markDirty()
    }
}