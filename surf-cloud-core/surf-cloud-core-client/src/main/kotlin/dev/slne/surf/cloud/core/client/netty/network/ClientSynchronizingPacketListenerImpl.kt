package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.sync.SyncRegistryImpl
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.*
import dev.slne.surf.cloud.core.common.plugin.task.CloudSynchronizeTaskManager
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch

class ClientSynchronizingPacketListenerImpl(
    val client: ClientNettyClientImpl,
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val statusUpdater: AbstractStatusUpdater,
) : ClientCommonPacketListenerImpl(connection), ClientSynchronizingPacketListener {

    private val log = logger()

    fun startSynchronizing() {
        statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZING)

        BeforeStartTaskScope.launch {
            CloudSynchronizeTaskManager.executeTasks(client)

            statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZE_WAIT_FOR_SERVER)
            send(FinishSynchronizingPacket)
        }
    }

    override suspend fun handleSynchronizeFinish(packet: ClientboundSynchronizeFinishPacket) {
        statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZED)

        val listener = ClientRunningPacketListenerImpl(connection, client, platformExtension)
        connection.setupInboundProtocol(RunningProtocols.CLIENTBOUND, listener)
        connection.send(ServerboundSynchronizeFinishAcknowledgedPacket)
        connection.setupOutboundProtocol(PreRunningProtocols.SERVERBOUND)

        client.initListener(listener)
        statusUpdater.switchState(AbstractStatusUpdater.State.CONNECTED)
        client.synchronizeCallback.complete(Unit)
    }

    override fun handleSyncValueChange(packet: SyncValueChangePacket) {
        try {
            if (!packet.registered) return
            SyncRegistryImpl.instance.updateSyncValue(packet.syncId, packet.value)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to update sync value for packet $packet")
        }
    }

    override fun handleBatchSyncValue(packet: ClientboundBatchSyncValuePacket) {
        try {
            SyncRegistryImpl.instance.applyBatchSyncValue(packet.syncValues)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to apply batch sync values for packet $packet")
        }
    }

    override fun handleBatchSyncSet(packet: ClientboundBatchSyncSetPacket) {
        try {
            SyncRegistryImpl.instance.applyBatchSyncSets(packet.syncSets)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to apply batch sync sets for packet $packet")
        }
    }

    override fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        try {
            SyncRegistryImpl.instance.handleSyncSetDelta(packet)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync set delta for packet $packet")
        }
    }

    override fun handlePacket(packet: NettyPacket) {
        TODO("Not yet implemented")
    }

    override fun restart() {
        platformExtension.restart()
    }

    override fun shutdown() {
        platformExtension.shutdown()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}