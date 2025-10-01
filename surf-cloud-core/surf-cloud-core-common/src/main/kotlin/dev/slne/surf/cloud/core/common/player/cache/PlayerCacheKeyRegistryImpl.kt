package dev.slne.surf.cloud.core.common.player.cache

import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import dev.slne.surf.cloud.api.common.player.cache.key.PlayerCacheKeyRegistry
import net.kyori.adventure.key.Key
import org.springframework.stereotype.Component

@Component
class PlayerCacheKeyRegistryImpl: PlayerCacheKeyRegistry {
    override fun allKeys(): List<CacheKey<*>> {
        TODO("Not yet implemented")
    }

    override fun register(key: CacheKey<*>) {
        TODO("Not yet implemented")
    }

    override fun byNetworkKey(networkKey: CacheNetworkKey): CacheKey<*>? {
        TODO("Not yet implemented")
    }

    override fun intKey(key: Key): CacheKey.Value<Int> {
        TODO("Not yet implemented")
    }

    override fun longKey(key: Key): CacheKey.Value<Long> {
        TODO("Not yet implemented")
    }

    override fun doubleKey(key: Key): CacheKey.Value<Double> {
        TODO("Not yet implemented")
    }

    override fun floatKey(key: Key): CacheKey.Value<Float> {
        TODO("Not yet implemented")
    }

    override fun booleanKey(key: Key): CacheKey.Value<Boolean> {
        TODO("Not yet implemented")
    }

    override fun stringKey(key: Key): CacheKey.Value<String> {
        TODO("Not yet implemented")
    }

    override fun <T : Enum<T>> enumKey(
        key: Key,
        enumClass: Class<T>
    ): CacheKey.Value<T> {
        TODO("Not yet implemented")
    }

    override fun <E : Any> listKey(key: Key): CacheKey.List<E> {
        TODO("Not yet implemented")
    }

    override fun <E : Any> setKey(key: Key): CacheKey.Set<E> {
        TODO("Not yet implemented")
    }

    override fun <K : Any, V : Any> mapKey(key: Key): CacheKey.Map<K, V> {
        TODO("Not yet implemented")
    }
}