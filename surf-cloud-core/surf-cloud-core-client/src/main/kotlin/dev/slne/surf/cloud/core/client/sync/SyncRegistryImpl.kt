package dev.slne.surf.cloud.core.client.sync

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.sync.SyncRegistry
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncMapDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.sync.BasicSyncValue
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncMapImpl
import dev.slne.surf.cloud.core.common.sync.SyncSetImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf
import dev.slne.surf.surfapi.core.api.util.synchronize

@AutoService(SyncRegistry::class)
class SyncRegistryImpl : CommonSyncRegistryImpl() {
    private val log = logger()
    private val lastChangeIds = mutableObject2LongMapOf<String>().synchronize()

    override fun afterChange(syncValue: BasicSyncValue<*>) {
        super.afterChange(syncValue)
        SyncValueChangePacket(CloudProperties.SERVER_NAME, syncValue).fireAndForget()
    }

    override fun <T> afterChange(
        syncSet: SyncSetImpl<T>,
        added: Boolean,
        changeId: Long,
        element: T
    ) {
        super.afterChange(syncSet, added, changeId, element)
        SyncSetDeltaPacket(syncSet, added, changeId, element).fireAndForget()
    }

    override fun <K, V> afterChange(
        syncMap: SyncMapImpl<K, V>,
        key: K,
        oldValue: V?,
        newValue: V?,
        changeId: Long
    ) {
        super.afterChange(syncMap, key, oldValue, newValue, changeId)
        SyncMapDeltaPacket(syncMap, key, oldValue, newValue, changeId).fireAndForget()
    }

    fun applyBatchSyncValue(syncValues: List<Pair<String, Any?>>) {
        require(frozen) { "SyncRegistry is not frozen, cannot apply batch" }

        syncValues.forEach { (syncId, value) ->
            updateSyncValue(syncId, value)
        }
    }

    fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        if (!packet.registered) return
        val set =
            getSet<Any?>(packet.setId)
                ?: error("SyncSet with id '${packet.setId}' is not registered")
        val lastChangeId = lastChangeIds.getLong(packet.setId)
        if (packet.changeId <= lastChangeId) {
            log.atInfo()
                .log("Ignoring stale SyncSetDeltaPacket for set '${packet.setId}' with changeId ${packet.changeId}, last known changeId is $lastChangeId")
            return
        }

        if (packet.added) {
            set.addInternal(packet.element)
        } else {
            set.removeInternal(packet.element)
        }

        lastChangeIds[packet.setId] = packet.changeId
    }

    fun applyBatchSyncSets(bulk: List<Pair<String, Set<Any?>>>) {
        bulk.forEach { (id, snapshot) ->
            val set = getSet<Any?>(id)
            if (set != null) {
                set.addAllInternal(snapshot)
                lastChangeIds[id] = Long.MAX_VALUE // Reset change ID to max after bulk update
            }
        }
    }

    fun handleSyncMapDelta(packet: SyncMapDeltaPacket) {
        if (!packet.registered) return
        val map =
            getMap<Any?, Any?>(packet.mapId)
                ?: error("SyncMap with id '${packet.mapId}' is not registered")
        val lastChangeId = lastChangeIds.getLong(packet.mapId)
        if (packet.changeId <= lastChangeId) {
            log.atInfo()
                .log("Ignoring stale SyncMapDeltaPacket for map '${packet.mapId}' with changeId ${packet.changeId}, last known changeId is $lastChangeId")
            return
        }

        if (packet.newValue != null) {
            map.putInternal(packet.key, packet.newValue)
        } else {
            map.removeInternal(packet.key)
        }

        lastChangeIds[packet.mapId] = packet.changeId
    }

    fun applyBatchSyncMaps(bulk: List<Pair<String, Map<Any?, Any?>>>) {
        bulk.forEach { (id, snapshot) ->
            val map = getMap<Any?, Any?>(id)
            if (map != null) {
                map.putAllInternal(snapshot)
                lastChangeIds[id] = Long.MAX_VALUE // Reset change ID to max after bulk update
            }
        }
    }

    companion object {
        val instance by lazy { CommonSyncRegistryImpl.instance as SyncRegistryImpl }
    }
}