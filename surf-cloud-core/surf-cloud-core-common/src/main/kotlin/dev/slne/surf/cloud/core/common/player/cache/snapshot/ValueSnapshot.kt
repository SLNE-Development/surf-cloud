package dev.slne.surf.cloud.core.common.player.cache.snapshot

import dev.slne.surf.cloud.api.common.player.cache.CacheKeyImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("value")
data class ValueSnapshot<T : Any>(
    override val key: CacheKeyImpl<T>,
    val typeId: String,        // serializer.descriptor.serialName
    val payload: ByteArray     // serialized T
) : PlayerCacheEntrySnapshot