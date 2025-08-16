package dev.slne.surf.cloud.core.common.player.cache.snapshot

import dev.slne.surf.cloud.api.common.player.cache.CacheKeyImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("map")
data class MapSnapshot<K : Any, V : Any>(
    override val key: CacheKeyImpl<MutableMap<K, V>>,
    val keyTypeId: String,
    val valueTypeId: String,
    val entries: List<Pair<ByteArray, ByteArray>>
) : PlayerCacheEntrySnapshot