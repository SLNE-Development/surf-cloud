package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientboundSynchronizeFinishPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.FinishSynchronizingPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ServerSynchronizingPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ServerboundSynchronizeFinishAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.plugin.task.CloudSynchronizeTaskManager
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ProxySecretHolder
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.cloud.standalone.netty.server.network.config.SynchronizeRegistriesTask
import dev.slne.surf.cloud.standalone.netty.server.network.config.SynchronizeUserTask
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.cloud.standalone.sync.SyncRegistryImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch

class ServerSynchronizingPacketListenerImpl(
    server: NettyServerImpl,
    connection: ConnectionImpl,
    client: ServerClientImpl,
    val proxy: Boolean
) : ServerCommonPacketListenerImpl(server, client, connection), ServerSynchronizingPacketListener {

    private val log = logger()

    @Volatile
    var state: State = State.START
        private set

    @Volatile
    private var clientFinishedSynchronizing = false

    override suspend fun tick0() {
        super.tick0()
        if (clientFinishedSynchronizing && state == State.WAIT_FOR_CLIENT) {
            finalize()
        }
    }

    fun startSynchronizing() {
        check(state == State.START) { "Cannot start synchronizing from state $state" }
        state = State.SYNCHRONIZING

        BeforeStartTaskScope.launch {
            send(ClientboundSetVelocitySecretPacket(ProxySecretHolder.currentSecret()))
            send(ClientboundBatchUpdateServer(serverManagerImpl.retrieveAllServers()))

            SynchronizeRegistriesTask.execute(client)
            SynchronizeUserTask.execute(client)
            CloudSynchronizeTaskManager.executeTasks(client)

            state = State.WAIT_FOR_CLIENT
        }
    }

    private fun finalize() {
        state = State.FINALIZING
        connection.send(ClientboundSynchronizeFinishPacket)
    }

    override fun handleFinishSynchronizing(packet: FinishSynchronizingPacket) {
        clientFinishedSynchronizing = true
    }

    override fun handleSynchronizeFinishAcknowledged(packet: ServerboundSynchronizeFinishAcknowledgedPacket) {
        check(state == State.FINALIZING) { "Unexpected synchronize finish acknowledgement packet" }

        PacketHandlerScope.launch {
            connection.setupOutboundProtocol(RunningProtocols.CLIENTBOUND)
            val listener = ServerRunningPacketListenerImpl(server, client, connection)
            connection.setupInboundProtocol(RunningProtocols.SERVERBOUND, listener)
            client.initListener(listener)
            server.registerClient(client, proxy)
            state = State.SYNCHRONIZED
        }
    }

    override fun handleSyncValueChange(packet: SyncValueChangePacket) {
        try {
            SyncRegistryImpl.instance.updateSyncValue(packet.syncId, packet.value)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to update sync value for packet $packet")
        }
    }

    override fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        try {
            SyncRegistryImpl.instance.handleSyncSetDeltaPacket(packet, connection)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync set delta for packet $packet")
        }
    }

    override fun handleCreateOfflineCloudPlayerIfNotExists(packet: ServerboundCreateOfflineCloudPlayerIfNotExistsPacket) {
        CloudPlayerManager.getOfflinePlayer(packet.uuid, true)
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


    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }

    enum class State {
        START,
        SYNCHRONIZING,
        WAIT_FOR_CLIENT,
        FINALIZING,
        SYNCHRONIZED,
    }
}