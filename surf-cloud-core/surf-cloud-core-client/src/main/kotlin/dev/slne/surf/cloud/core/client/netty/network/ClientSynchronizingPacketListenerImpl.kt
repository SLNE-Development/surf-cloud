package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.server.ClientCloudServerImpl
import dev.slne.surf.cloud.core.client.server.ClientProxyCloudServerImpl
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.client.sync.SyncRegistryImpl
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundBatchUpdateServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.*
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
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
        connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)

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

    override suspend fun handleBatchUpdateServer(packet: ClientboundBatchUpdateServer) {
        serverManagerImpl.batchUpdateServer(packet.servers.map { data ->
            if (data.proxy) {
                ClientProxyCloudServerImpl(data.serverId, data.group, data.name, data.playAddress)
            } else {
                ClientCloudServerImpl(
                    data.serverId,
                    data.group,
                    data.name,
                    data.playAddress,
                    data.lobby
                ).also { server ->
                    platformExtension.registerCloudServerToProxy(server)
                }
            }
        })
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

    override fun handleSetVelocitySecret(packet: ClientboundSetVelocitySecretPacket) {
        try {
            client.velocitySecret = packet.secret
            platformExtension.setVelocitySecret(packet.secret)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to set velocity secret for packet $packet")
        }
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val info = NettyPacketInfo(connection, ConnectionProtocol.SYNCHRONIZING)

        for (listener in listeners) {
            PacketHandlerScope.launch {
                try {
                    listener.handle(packet, info)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener.owner::class.simpleName,
                            packet::class.simpleName
                        )
                }
            }
        }
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