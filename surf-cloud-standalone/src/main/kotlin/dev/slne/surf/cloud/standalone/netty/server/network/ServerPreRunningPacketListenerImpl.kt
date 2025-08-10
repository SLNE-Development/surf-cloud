package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.SynchronizingProtocols
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager

class ServerPreRunningPacketListenerImpl(
    server: NettyServerImpl,
    connection: ConnectionImpl,
    client: ServerClientImpl,
    val proxy: Boolean
) : ServerCommonPacketListenerImpl(server, client, connection), ServerPreRunningPacketListener {
    private var state = State.PRE_RUNNING

    init {
        bean<PluginInitializerManager>().pluginsEnabledDeferred.invokeOnCompletion {
            finishPreRunning()
        }
    }

    private fun finishPreRunning() {
        check(state == State.PRE_RUNNING) { "Cannot finish pre-running from $state" }
        state = State.FINISHING_PRE_RUNNING

        connection.send(ClientboundPreRunningFinishedPacket)
    }

    override suspend fun handlePreRunningAcknowledged(packet: ServerboundPreRunningAcknowledgedPacket) {
        check(state == State.FINISHING_PRE_RUNNING) { "Cannot proceed to pre-running acknowledged state from $state" }
        state = State.PRE_RUNNING_ACKNOWLEDGED

        // Everything is done on both sides. Now the server waits for the client to be ready to run.
    }

    override fun handleRequestContinuation(packet: ServerboundRequestContinuation) {
        check(state == State.PRE_RUNNING_ACKNOWLEDGED) { "Cannot proceed to running state from $state" }

        send(ClientboundProceedToSynchronizingPacket)
    }

    override suspend fun handleReadyToRun(packet: ServerboundProceedToSynchronizingAcknowledgedPacket) {
        check(state == State.PRE_RUNNING_ACKNOWLEDGED) { "Cannot proceed to running state from $state" }

        connection.setupOutboundProtocol(SynchronizingProtocols.CLIENTBOUND)
        val listener = ServerSynchronizingPacketListenerImpl(server, connection, client, proxy)
        connection.setupInboundProtocol(
            SynchronizingProtocols.SERVERBOUND,
            listener
        )

        client.playAddress = packet.playAddress
        listener.startSynchronizing()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }

    private enum class State {
        PRE_RUNNING,
        FINISHING_PRE_RUNNING,
        PRE_RUNNING_ACKNOWLEDGED
    }
}