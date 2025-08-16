package dev.slne.surf.cloud.api.common.player.cache

interface CloudPlayerCacheBatch {
    fun <T : Any> set(key: CacheKey.Value<T>, value: T)
    fun remove(key: CacheKey<*>)

    fun <E : Any> listAdd(
        key: CacheKey.List<E>,
        element: E,
        index: Int? = null
    )

    fun <E : Any> listRemoveAt(
        key: CacheKey.List<E>,
        index: Int
    )

    fun <E : Any> setAdd(
        key: CacheKey.Set<E>,
        element: E,
    )

    fun <E : Any> setRemove(
        key: CacheKey.Set<E>,
        element: E,
    )

    fun <K : Any, V : Any> mapPut(
        key: CacheKey.Map<K, V>,
        k: K,
        v: V,
    )

    fun <K : Any, V : Any> mapRemove(
        key: CacheKey.Map<K, V>,
        k: K,
    )
}