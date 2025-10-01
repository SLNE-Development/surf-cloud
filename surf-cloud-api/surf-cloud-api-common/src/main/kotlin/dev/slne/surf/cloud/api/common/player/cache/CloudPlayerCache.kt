package dev.slne.surf.cloud.api.common.player.cache

import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import org.jetbrains.annotations.ApiStatus
import java.util.*

typealias ValueCacheChangeListener<T> = (old: T, new: T) -> Unit

@ApiStatus.NonExtendable
interface CloudPlayerCache {
    val uuid: UUID

    operator fun <T : Any> get(key: CacheKey<T>): T?
    operator fun <T : Any> set(key: CacheKey.Value<T>, value: T)

    fun <T : Any> getOrPut(key: CacheKey.Value<T>, default: () -> T): T {
        return this[key] ?: default().also { this[key] = it }
    }

    fun remove(key: CacheKey<*>)
}