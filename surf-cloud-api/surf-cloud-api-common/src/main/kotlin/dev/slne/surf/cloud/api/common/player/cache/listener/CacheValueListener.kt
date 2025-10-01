package dev.slne.surf.cloud.api.common.player.cache.listener

import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import java.util.*

fun interface CacheValueListener<T : Any> {
    /**
     * Invoked when the cached value associated with a specific player and cache key is changed.
     *
     * @param playerUuid The unique identifier of the player whose cache value has changed.
     * @param key The cache key identifying the value associated with the change.
     * @param oldValue The previous value associated with the cache key, or null if no value was set.
     * @param newValue The new value being updated in the cache, or null if the value is removed.
     */
    fun onCacheValueChanged(playerUuid: UUID, key: CacheKey<T>, oldValue: T?, newValue: T?)
}