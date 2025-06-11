package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.sync.SyncValue
import dev.slne.surf.cloud.api.common.sync.SyncValueListener
import dev.slne.surf.surfapi.core.api.util.logger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

open class BasicSyncValue<T> internal constructor(
    override val id: String,
    defaultValue: T,
    override val codec: StreamCodec<SurfByteBuf, T>
) : SyncValue<T> {
    private val value = AtomicReference(defaultValue)
    private val listeners = CopyOnWriteArrayList<SyncValueListener<T>>()

    init {
        CommonSyncRegistryImpl.instance.register(this)
    }

    override fun get(): T = value.get()

    override fun set(newValue: T) {
        val oldValue = value.getAndSet(newValue)
        if (oldValue != newValue) {
            callListeners(oldValue, newValue)
            CommonSyncRegistryImpl.instance.afterChange(this)
        }
    }

    override fun subscribe(listener: SyncValueListener<T>) = listeners.addIfAbsent(listener)
    override fun rateLimited(minInterval: Duration): SyncValue<T> =
        RateLimitedSyncValue(this, minInterval)


    @Suppress("UNCHECKED_CAST")
    fun internalSet(newValue: Any?) {
        val castedValue = newValue as? T
            ?: error("Cannot cast value '$newValue' to type '${value.get()::class.simpleName}'")
        val oldValue = value.getAndSet(castedValue)
        if (oldValue != castedValue) {
            callListeners(oldValue, castedValue)
        }
    }

    private fun callListeners(oldValue: T, newValue: T) {
        for (handler in listeners) {
            try {
                handler(oldValue, newValue)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Error while notifying listener for SyncValue '$id'")
            }
        }
    }

    companion object {
        private val log = logger()
    }
}