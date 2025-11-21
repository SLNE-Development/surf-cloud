package dev.slne.surf.cloud.api.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService

@InternalApi
interface SyncRegistry {

    fun <T> createSyncValue(
        id: String,
        defaultValue: T,
        codec: StreamCodec<SurfByteBuf, T>
    ): SyncValue<T>

    fun <T> createSyncSet(
        id: String,
        codec: StreamCodec<SurfByteBuf, T>
    ): SyncSet<T>

    fun <K, V> createSyncMap(
        id: String,
        keyCodec: StreamCodec<SurfByteBuf, K>,
        valueCodec: StreamCodec<SurfByteBuf, V>
    ): SyncMap<K, V>

    companion object {
        @InternalApi
        val instance = requiredService<SyncRegistry>()
    }
}