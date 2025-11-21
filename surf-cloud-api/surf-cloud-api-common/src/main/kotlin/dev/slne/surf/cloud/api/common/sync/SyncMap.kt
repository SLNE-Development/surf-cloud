package dev.slne.surf.cloud.api.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable

typealias SyncMapListener<K, V> = (key: K, oldValue: V?, newValue: V?) -> Unit

interface SyncMap<K, V> : Object2ObjectMap<K, V> {
    val id: String

    /**
     * There is generally no need to use this codec directly, as this sync map should always
     * be automatically synchronized across the network.
     */
    @get:ApiStatus.Obsolete
    val codec: StreamCodec<SurfByteBuf, Map<K, V>>

    fun subscribe(listener: SyncMapListener<K, V>): Boolean
    fun snapshot(): @Unmodifiable Object2ObjectMap<K, V>

    companion object {
        operator fun <K, V> invoke(
            id: String,
            keyCodec: StreamCodec<SurfByteBuf, K>,
            valueCodec: StreamCodec<SurfByteBuf, V>
        ): SyncMap<K, V> = of(id, keyCodec, valueCodec)

        inline operator fun <reified K, reified V> invoke(id: String): SyncMap<K, V> = serializable(id)
        operator fun <K, V> invoke(
            id: String,
            keySerializer: KSerializer<K>,
            valueSerializer: KSerializer<V>
        ): SyncMap<K, V> = serializable(id, keySerializer, valueSerializer)

        inline fun <reified K, reified V> serializable(
            id: String,
        ): SyncMap<K, V> = serializable(id, serializer(), serializer())

        fun <K, V> serializable(
            id: String,
            keySerializer: KSerializer<K>,
            valueSerializer: KSerializer<V>,
        ): SyncMap<K, V> = of(
            id,
            SurfByteBuf.streamCodecFromKotlin(keySerializer),
            SurfByteBuf.streamCodecFromKotlin(valueSerializer)
        )

        fun <K, V> of(
            id: String,
            keyCodec: StreamCodec<SurfByteBuf, K>,
            valueCodec: StreamCodec<SurfByteBuf, V>
        ): SyncMap<K, V> =
            SyncRegistry.instance.createSyncMap(id, keyCodec, valueCodec)
    }
}
