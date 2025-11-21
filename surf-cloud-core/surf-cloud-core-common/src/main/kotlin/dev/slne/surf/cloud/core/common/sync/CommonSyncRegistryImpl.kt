package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.sync.SyncMap
import dev.slne.surf.cloud.api.common.sync.SyncRegistry
import dev.slne.surf.cloud.api.common.sync.SyncSet
import dev.slne.surf.cloud.api.common.sync.SyncValue
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class CommonSyncRegistryImpl : SyncRegistry {
    protected var frozen = false
        private set

    protected val syncValues = mutableObject2ObjectMapOf<String, BasicSyncValue<*>>()
    protected val syncSets = mutableObject2ObjectMapOf<String, SyncSetImpl<*>>()
    protected val syncMaps = mutableObject2ObjectMapOf<String, SyncMapImpl<*, *>>()

    fun freeze() {
        require(!frozen) { "SyncRegistry is already frozen" }
        frozen = true
    }

    fun register(syncValue: BasicSyncValue<*>) {
        require(!frozen) { "SyncRegistry is frozen and cannot accept new SyncValues" }

        val previous = syncValues.put(syncValue.id, syncValue)
        check(previous == null) { "SyncValue with id '${syncValue.id}' was already registered" }
    }

    fun <T> register(syncSet: SyncSetImpl<T>) {
        require(!frozen) { "SyncRegistry is frozen and cannot accept new SyncSets" }

        val previous = syncSets.put(syncSet.id, syncSet)
        check(previous == null) { "SyncSet with id '${syncSet.id}' was already registered" }
    }

    fun <K, V> register(syncMap: SyncMapImpl<K, V>) {
        require(!frozen) { "SyncRegistry is frozen and cannot accept new SyncMaps" }

        val previous = syncMaps.put(syncMap.id, syncMap)
        check(previous == null) { "SyncMap with id '${syncMap.id}' was already registered" }
    }

    @OverridingMethodsMustInvokeSuper
    open fun afterChange(syncValue: BasicSyncValue<*>) {
        require(frozen) { "SyncRegistry is not frozen, cannot process afterChange" }
        require(syncValue.id in syncValues) { "SyncValue with id '${syncValue.id}' is not registered" }
    }

    @OverridingMethodsMustInvokeSuper
    open fun <T> afterChange(
        syncSet: SyncSetImpl<T>,
        added: Boolean,
        changeId: Long,
        element: T
    ) {
        require(frozen) { "SyncRegistry is not frozen, cannot process afterChange" }
        require(syncSet.id in syncSets) { "SyncSet with id '${syncSet.id}' is not registered" }
    }

    @OverridingMethodsMustInvokeSuper
    open fun <K, V> afterChange(
        syncMap: SyncMapImpl<K, V>,
        key: K,
        oldValue: V?,
        newValue: V?,
        changeId: Long
    ) {
        require(frozen) { "SyncRegistry is not frozen, cannot process afterChange" }
        require(syncMap.id in syncMaps) { "SyncMap with id '${syncMap.id}' is not registered" }
    }

    fun getSyncValueCodec(syncId: String): StreamCodec<SurfByteBuf, Any?>? {
        require(frozen) { "SyncRegistry is not frozen, cannot get codec" }

        val syncValue = syncValues[syncId] ?: return null
        return syncValue.codec as? StreamCodec<SurfByteBuf, Any?>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSet(id: String): SyncSetImpl<T>? = syncSets[id] as? SyncSetImpl<T>

    @Suppress("UNCHECKED_CAST")
    fun <K, V> getMap(id: String): SyncMapImpl<K, V>? = syncMaps[id] as? SyncMapImpl<K, V>

    fun updateSyncValue(syncId: String, value: Any?) {
        require(frozen) { "SyncRegistry is not frozen, cannot update SyncValue" }

        val syncValue = syncValues[syncId] ?: error("SyncValue with id '$syncId' is not registered")
        syncValue.internalSet(value)
    }

    override fun <T> createSyncValue(
        id: String,
        defaultValue: T,
        codec: StreamCodec<SurfByteBuf, T>
    ): SyncValue<T> {
        return BasicSyncValue(id, defaultValue, codec)
    }

    override fun <T> createSyncSet(
        id: String,
        codec: StreamCodec<SurfByteBuf, T>
    ): SyncSet<T> {
        return SyncSetImpl(id, codec)
    }

    override fun <K, V> createSyncMap(
        id: String,
        keyCodec: StreamCodec<SurfByteBuf, K>,
        valueCodec: StreamCodec<SurfByteBuf, V>
    ): SyncMap<K, V> {
        return SyncMapImpl(id, keyCodec, valueCodec)
    }

    companion object {
        val instance by lazy { SyncRegistry.instance as CommonSyncRegistryImpl }
    }
}
