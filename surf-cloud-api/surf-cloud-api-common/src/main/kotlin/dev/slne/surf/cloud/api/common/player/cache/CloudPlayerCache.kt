package dev.slne.surf.cloud.api.common.player.cache

import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface CloudPlayerCache {
    val uuid: UUID

    operator fun <T : Any> set(key: CacheKey.Value<T>, value: T)
    operator fun <T : Any> get(key: CacheKey.Value<T>): T?
    fun remove(key: CacheKey<*>)

    fun <E : Any> list(key: CacheKey.List<E>): CacheList<E>

    fun <E : Any> set(
        key: CacheKey.Set<E>,
    ): CacheSet<E>

    fun <K : Any, V : Any> map(
        key: CacheKey.Map<K, V>,
    ): CacheMap<K, V>

    fun <T : Any, D : Any> structured(
        key: CacheKey.Structured<T, D>,
        type: StructuredType<T, D>,
        default: () -> T
    ): CacheStructured<T, D>

    fun <R> batch(block: CloudPlayerCacheBatch.() -> R): R

    fun clear()
}