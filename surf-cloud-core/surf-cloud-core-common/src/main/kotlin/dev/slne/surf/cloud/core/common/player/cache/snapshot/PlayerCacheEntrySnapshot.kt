package dev.slne.surf.cloud.core.common.player.cache.snapshot

import dev.slne.surf.cloud.api.common.player.cache.CacheKeyImpl
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerCacheEntrySnapshot {
    val key: CacheKeyImpl<@Suppress("unused") Any>
}