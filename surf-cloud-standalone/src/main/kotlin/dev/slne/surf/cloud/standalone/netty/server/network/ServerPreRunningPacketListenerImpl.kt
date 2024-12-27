package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ClientboundPreRunningFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ClientboundReadyToRunPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerPreRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerboundPreRunningAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerboundReadyToRunPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerboundRequestContinuation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

class ServerPreRunningPacketListenerImpl(
    server: NettyServerImpl,
    connection: ConnectionImpl,
    client: ServerClientImpl,
    val proxy: Boolean
) : ServerCommonPacketListenerImpl(server, client, connection), ServerPreRunningPacketListener {
    private var state = State.PRE_RUNNING

    init {
        // currently no work to do here
        finishPreRunning()
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

        send(ClientboundReadyToRunPacket)
    }

    override suspend fun handleReadyToRun(packet: ServerboundReadyToRunPacket) {
        check(state == State.PRE_RUNNING_ACKNOWLEDGED) { "Cannot proceed to running state from $state" }

        connection.setupOutboundProtocol(RunningProtocols.CLIENTBOUND)
        val listener = ServerRunningPacketListenerImpl(server, client, connection)
        connection.setupInboundProtocol(
            RunningProtocols.SERVERBOUND,
            listener
        )
        client.initListener(listener)
        server.registerClient(client, proxy)
    }

    private enum class State {
        PRE_RUNNING,
        FINISHING_PRE_RUNNING,
        PRE_RUNNING_ACKNOWLEDGED
    }
}