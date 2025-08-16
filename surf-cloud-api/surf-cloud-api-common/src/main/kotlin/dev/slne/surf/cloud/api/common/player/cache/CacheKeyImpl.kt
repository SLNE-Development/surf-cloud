package dev.slne.surf.cloud.api.common.player.cache

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.SerializableKey
import kotlinx.serialization.Serializable

@Serializable
data class CacheKeyImpl<T : Any>(private val backing: SerializableKey) : CacheKey<T> {
    override fun namespace() = backing.namespace()
    override fun value() = backing.value()
    override fun asString() = backing.asString()
}