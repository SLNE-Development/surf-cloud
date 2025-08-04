package dev.slne.surf.cloud.api.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable

typealias SyncSetListener<T> = (added: Boolean, element: T) -> Unit

interface SyncSet<T> : ObjectSet<T> {
    val id: String

    /**
     * There is generally no need to use this codec directly, as this sync set should always
     * be automatically synchronized across the network.
     */
    @get:ApiStatus.Obsolete
    val codec: StreamCodec<SurfByteBuf, Set<T>>

    fun subscribe(listener: SyncSetListener<T>): Boolean
    fun snapshot(): @Unmodifiable ObjectSet<T>

    companion object {
        operator fun <T> invoke(
            id: String,
            codec: StreamCodec<SurfByteBuf, T>
        ): SyncSet<T> = of(id, codec)

        inline operator fun <reified T> invoke(id: String): SyncSet<T> = serializable(id)
        operator fun <T> invoke(
            id: String,
            serializer: KSerializer<T>
        ): SyncSet<T> = serializable(id, serializer)

        inline fun <reified T> serializable(
            id: String,
        ): SyncSet<T> = serializable(id, serializer())

        fun <T> serializable(
            id: String,
            serializer: KSerializer<T>,
        ): SyncSet<T> = of(
            id,
            SurfByteBuf.streamCodecFromKotlin(serializer)
        )

        fun <T> of(id: String, codec: StreamCodec<SurfByteBuf, T>): SyncSet<T> =
            SyncRegistry.instance.createSyncSet(id, codec)
    }
}