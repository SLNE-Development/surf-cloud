package dev.slne.surf.cloud.core.common.player.cache.entry

import dev.slne.surf.cloud.api.common.player.cache.CacheKey

internal sealed interface EntryMeta {
    val changeId: Long

    data class Value<T : Any>(val key: CacheKey.Value<T>, override val changeId: Long) : EntryMeta
    data class List<E : Any>(val key: CacheKey.List<E>, override val changeId: Long) : EntryMeta
    data class Set<E : Any>(val key: CacheKey.Set<E>, override val changeId: Long) : EntryMeta
    data class Map<K : Any, V : Any>(val key: CacheKey.Map<K, V>, override val changeId: Long) :
        EntryMeta

    data class Structured<T : Any, D : Any>(
        val key: CacheKey.Structured<T, D>,
        override val changeId: Long
    ) : EntryMeta

    data class Removed<T : Any, K : CacheKey<T>>(val key: K, override val changeId: Long) :
        EntryMeta
}