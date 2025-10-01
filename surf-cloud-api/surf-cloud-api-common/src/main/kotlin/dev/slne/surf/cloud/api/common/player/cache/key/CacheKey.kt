package dev.slne.surf.cloud.api.common.player.cache.key

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentMap

sealed interface CacheKey<T : Any> : Key {

    val persistent: Boolean

    fun toNetworkKey() = CacheNetworkKey.wrap(this)

    sealed interface Value<T : Any> : CacheKey<T> {
        val codec: StreamCodec<SurfByteBuf, T>
    }

    sealed interface List<E : Any> : CacheKey<MutableList<E>> {
        val elementCodec: StreamCodec<SurfByteBuf, E>
    }

    sealed interface Set<E : Any> : CacheKey<MutableSet<E>> {
        val elementCodec: StreamCodec<SurfByteBuf, E>
    }

    sealed interface Map<K : Any, V : Any> : CacheKey<ConcurrentMap<K, V>> {
        val keyCodec: StreamCodec<SurfByteBuf, K>
        val valueCodec: StreamCodec<SurfByteBuf, V>
    }

    sealed interface Structured<T : Any, D : Any> : CacheKey<T> {
        val deltaCodec: StreamCodec<SurfByteBuf, D>
    }
}

