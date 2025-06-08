package dev.slne.surf.cloud.standalone.sync

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.sync.SyncRegistry
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.sync.BasicSyncValue
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncSetImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf

@AutoService(SyncRegistry::class)
class SyncRegistryImpl : CommonSyncRegistryImpl() {
    private val log = logger()
    private val lastChangeIds = mutableObject2LongMapOf<String>()

    override fun afterChange(syncValue: BasicSyncValue<*>) {
        super.afterChange(syncValue)
        broadcast(SyncValueChangePacket(null, syncValue))
    }

    override fun <T> afterChange(
        syncSet: SyncSetImpl<T>,
        added: Boolean,
        changeId: Long,
        element: T
    ) {
        super.afterChange(syncSet, added, changeId, element)
        val lastChangeId = lastChangeIds.getLong(syncSet.id)
        if (changeId <= lastChangeId) {
            log.atInfo()
                .log("Ignoring stale SyncSetDeltaPacket for set '${syncSet.id}' with changeId $changeId, last known changeId is $lastChangeId")
            return
        }
        lastChangeIds[syncSet.id] = changeId
        broadcast(SyncSetDeltaPacket(syncSet, added, changeId, element))
    }

    fun handleChangePacket(packet: SyncValueChangePacket, sender: ConnectionImpl) {
        if (!packet.registered) return
        val syncId = packet.syncId
        val value = packet.value

        updateSyncValue(syncId, value)
        broadcast(packet, sender)
    }

    fun handleSyncSetDeltaPacket(packet: SyncSetDeltaPacket, sender: ConnectionImpl) {
        if (!packet.registered) return
        val set = getSet<Any?>(packet.id)
        if (set == null) {
            log.atWarning()
                .log("SyncSet with id '${packet.id}' not found, cannot apply delta")
            return
        }

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

        broadcast(packet, sender)

    }

    fun prepareBatchSyncValues(): Map<String, BasicSyncValue<*>> {
        require(frozen) { "SyncRegistry is not frozen, cannot prepare batch" }

        return syncValues.toMap()
    }

    fun prepareBatchSyncSets(): Map<String, SyncSetImpl<*>> {
        require(frozen) { "SyncRegistry is not frozen, cannot prepare batch sync set" }
        return syncSets.toMap()
    }

    private fun broadcast(packet: NettyPacket, sender: ConnectionImpl? = null) {
        if (sender == null) {
            bean<NettyServerImpl>().connection.broadcast(packet)
        } else {
            val protocols = packet.protocols
            for (connection in bean<NettyServerImpl>().connection.connections) {
                if (connection.outboundProtocolInfo.id !in protocols) continue
                if (connection == sender) continue // Skip the sender
                connection.send(packet)
            }
        }
    }

    companion object {
        val instance = CommonSyncRegistryImpl.instance as SyncRegistryImpl
    }
}