package dev.slne.surf.cloud.api.common.player.cache

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentMap

@Serializable
sealed interface CacheKey<T : Any> : Key {
    companion object {
        fun <T : Any> of(namespace: String, value: String): CacheKey<T> {
            return CacheKeyImpl(Key.key(namespace, value))
        }

        fun <T : Any> of(key: Key): CacheKey<T> {
            return CacheKeyImpl(key)
        }

        operator fun <T : Any> invoke(namespace: String, value: String): CacheKey<T> {
            return of(namespace, value)
        }

        operator fun <T : Any> invoke(key: Key): CacheKey<T> {
            return of(key)
        }
    }

    @Serializable
    sealed interface Value<T : Any> : CacheKey<T> {
        val codec: StreamCodec<SurfByteBuf, T>
    }

    @Serializable
    sealed interface List<E : Any> : CacheKey<MutableList<E>> {
        val elementCodec: StreamCodec<SurfByteBuf, E>
    }

    @Serializable
    sealed interface Set<E : Any> : CacheKey<MutableSet<E>> {
        val elementCodec: StreamCodec<SurfByteBuf, E>
    }

    @Serializable
    sealed interface Map<K : Any, V : Any> : CacheKey<ConcurrentMap<K, V>> {
        val keyCodec: StreamCodec<SurfByteBuf, K>
        val valueCodec: StreamCodec<SurfByteBuf, V>
    }

    @Serializable
    sealed interface Structured<T : Any, D: Any> : CacheKey<T>
}

