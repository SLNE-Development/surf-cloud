package dev.slne.surf.cloud.core.client.sync

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.sync.SyncRegistry
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.sync.BasicSyncValue
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf

@AutoService(SyncRegistry::class)
class SyncRegistryImpl : CommonSyncRegistryImpl() {
    private val log = logger()
    private val lastChangeIds = mutableObject2LongMapOf<String>()

    override fun afterChange(syncValue: BasicSyncValue<*>) {
        super.afterChange(syncValue)
        SyncValueChangePacket(CloudPersistentData.SERVER_ID, syncValue).fireAndForget()
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
            getSet<Any?>(packet.setId) ?: error("SyncSet with id '${packet.setId}' is not registered")
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

    companion object {
        val instance by lazy { CommonSyncRegistryImpl.instance as SyncRegistryImpl }
    }
}