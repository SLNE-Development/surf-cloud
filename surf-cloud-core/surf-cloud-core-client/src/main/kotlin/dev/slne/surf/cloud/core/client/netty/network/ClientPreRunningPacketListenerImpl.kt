package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.netty.network.AbstractStatusUpdater.State
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ClientPreRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.clientbound.ClientboundPreRunningFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.clientbound.ClientboundProceedToSynchronizingPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundPreRunningAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundProceedToSynchronizingAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundRequestContinuation
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.SynchronizingProtocols
import kotlinx.coroutines.CompletableDeferred

class ClientPreRunningPacketListenerImpl(
    val client: ClientNettyClientImpl,
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
        client.preRunningCallback.complete(Unit)
    }

    override suspend fun handlePreRunningFinished(packet: ClientboundPreRunningFinishedPacket) {
        finishPreRunning()
        connection.send(ServerboundPreRunningAcknowledgedPacket)
    }

    override suspend fun handleProceedToSynchronizing(packet: ClientboundProceedToSynchronizingPacket) {
        val listener = ClientSynchronizingPacketListenerImpl(
            client,
            connection,
            platformExtension,
            statusUpdater
        )
        connection.setupInboundProtocol(
            SynchronizingProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundProceedToSynchronizingAcknowledgedPacket(client.playAddress))
        connection.setupOutboundProtocol(SynchronizingProtocols.SERVERBOUND)

        completion.complete(Unit)
        listener.startSynchronizing()
    }


    fun proceedToSynchronizingState() {
        check(statusUpdater.getState() == State.PRE_RUNNING) { "Cannot proceed to running state from ${statusUpdater.getState()}" }
        send(ServerboundRequestContinuation)
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        throw FatalSurfError {
            simpleErrorMessage("Client disconnected from the server before the connection was fully established.")
            detailedErrorMessage("Reason: ${details.buildMessage()}")
            possibleSolution("Try restarting the client.")
        }
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