package dev.slne.surf.cloud.core.common.player.cache.snapshot

import dev.slne.surf.cloud.api.common.player.cache.CacheKeyImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("list")
data class ListSnapshot<E : Any>(
    override val key: CacheKeyImpl<MutableList<E>>,
    val elementTypeId: String,
    val elements: List<ByteArray>
) : PlayerCacheEntrySnapshot