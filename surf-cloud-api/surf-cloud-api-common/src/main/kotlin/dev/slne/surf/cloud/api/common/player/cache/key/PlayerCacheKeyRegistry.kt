package dev.slne.surf.cloud.api.common.player.cache.key

import net.kyori.adventure.key.Key

interface PlayerCacheKeyRegistry {
    fun allKeys(): List<CacheKey<*>>
    fun register(key: CacheKey<*>)
    fun byNetworkKey(networkKey: CacheNetworkKey): CacheKey<*>?

    fun intKey(key: Key): CacheKey.Value<Int>
    fun longKey(key: Key): CacheKey.Value<Long>
    fun doubleKey(key: Key): CacheKey.Value<Double>
    fun floatKey(key: Key): CacheKey.Value<Float>
    fun booleanKey(key: Key): CacheKey.Value<Boolean>
    fun stringKey(key: Key): CacheKey.Value<String>
    fun <T : Enum<T>> enumKey(key: Key, enumClass: Class<T>): CacheKey.Value<T>

    fun <E : Any> listKey(key: Key): CacheKey.List<E>
    fun <E : Any> setKey(key: Key): CacheKey.Set<E>
    fun <K : Any, V : Any> mapKey(key: Key): CacheKey.Map<K, V>
}