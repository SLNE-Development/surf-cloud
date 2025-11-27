package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.netty.network.AbstractStatusUpdater.State
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.SynchronizingProtocols
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class ClientPreRunningPacketListenerImpl(
    override val client: ClientNettyClientImpl,
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val statusUpdater: AbstractStatusUpdater,
) : ClientCommonPacketListenerImpl(connection), ClientPreRunningPacketListener {

    private val completion = CompletableDeferred<Unit>()

    init {
        client.startSynchronizeTask = {
            proceedToSynchronizingState()
            completion.await()
        }
    }

    private fun finishPreRunning() {
        statusUpdater.switchState(State.PRE_RUNNING)
        client.preRunningFinishedSignal.fire()
    }

    override fun handlePreRunningFinished(packet: ClientboundPreRunningFinishedPacket) {
        PacketHandlerScope.launch {
            finishPreRunning()
            connection.send(ServerboundPreRunningAcknowledgedPacket)
        }
    }

    override fun handleProceedToSynchronizing(packet: ClientboundProceedToSynchronizingPacket) {
        val listener = ClientSynchronizingPacketListenerImpl(
            client,
            connection,
            platformExtension,
            statusUpdater,
        )
        client.connectionManager.currentPacketLister = listener

        connection.setupInboundProtocol(
            SynchronizingProtocols.CLIENTBOUND,
            listener
        ).syncUninterruptibly()
        connection.send(ServerboundProceedToSynchronizingAcknowledgedPacket(client.playAddress))
        connection.setupOutboundProtocol(SynchronizingProtocols.SERVERBOUND).syncUninterruptibly()

        completion.complete(Unit)
        listener.startSynchronizing()
    }


    fun proceedToSynchronizingState() {
        check(statusUpdater.getState() == State.PRE_RUNNING) { "Cannot proceed to running state from ${statusUpdater.getState()}" }
        send(ServerboundRequestContinuation)
    }

    override fun restart() {
        platformExtension.restart()
    }

    override fun shutdown() {
        platformExtension.shutdown()
    }

    override fun isAcceptingMessages(): Boolean {
        return completion.isActive
    }
}