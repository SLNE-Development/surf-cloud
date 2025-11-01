package dev.slne.surf.cloud.api.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty
import kotlin.time.Duration

typealias SyncValueListener<T> = (old: T, new: T) -> Unit

interface SyncValue<T> {

    val id: String

    val codec: StreamCodec<SurfByteBuf, T>

    /**
     * Returns the current value of this sync value.
     */
    fun get(): T

    /**
     * Sets a new value for this sync value.
     */
    fun set(newValue: T)

    /**
     * Subscribes to changes in this sync value.
     */
    fun subscribe(listener: SyncValueListener<T>): Boolean

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) = set(newValue)

    fun rateLimited(minInterval: Duration): SyncValue<T>

    companion object {
        operator fun <T> invoke(
            id: String,
            defaultValue: T,
            codec: StreamCodec<SurfByteBuf, T>
        ): SyncValue<T> = of(id, defaultValue, codec)

        inline operator fun <reified T> invoke(id: String, defaultValue: T): SyncValue<T> =
            serializable(id, defaultValue)

        operator fun <T> invoke(
            id: String,
            defaultValue: T,
            serializer: KSerializer<T>
        ): SyncValue<T> = serializable(id, defaultValue, serializer)

        inline fun <reified T> serializable(
            id: String,
            defaultValue: T
        ): SyncValue<T> = serializable(id, defaultValue, serializer())

        fun <T> serializable(
            id: String,
            defaultValue: T,
            serializer: KSerializer<T>,
        ): SyncValue<T> = of(
            id,
            defaultValue,
            SurfByteBuf.streamCodecFromKotlin(serializer)
        )

        fun <T> of(id: String, defaultValue: T, codec: StreamCodec<SurfByteBuf, T>): SyncValue<T> =
            SyncRegistry.instance.createSyncValue(id, defaultValue, codec)
    }
}