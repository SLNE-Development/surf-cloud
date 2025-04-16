package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.netty.network.AbstractStatusUpdater.State
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import kotlinx.coroutines.CompletableDeferred

class ClientPreRunningPacketListenerImpl(
    val client: ClientNettyClientImpl,
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val statusUpdater: AbstractStatusUpdater,
    val awaitFinishPreRunning: CompletableDeferred<Unit>
) : ClientCommonPacketListenerImpl(connection), ClientPreRunningPacketListener {

    private val completion = CompletableDeferred<Unit>()

    init {
        client.finalizeHandler { proceedToRunningState() }
    }

    private fun finishPreRunning() {
        statusUpdater.switchState(State.PRE_RUNNING)
        awaitFinishPreRunning.complete(Unit)
    }

    override suspend fun handlePreRunningFinished(packet: ClientboundPreRunningFinishedPacket) {
        finishPreRunning()
        connection.send(ServerboundPreRunningAcknowledgedPacket)
    }

    override suspend fun handleReadyToRun(packet: ClientboundReadyToRunPacket) {
        val listener = ClientRunningPacketListenerImpl(connection, client, platformExtension)
        connection.setupInboundProtocol(
            RunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundReadyToRunPacket)
        connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)

        client.initListener(listener)
        statusUpdater.switchState(State.CONNECTED)
        completion.complete(Unit)
    }

    suspend fun proceedToRunningState() {
        check(statusUpdater.getState() == State.PRE_RUNNING) { "Cannot proceed to running state from ${statusUpdater.getState()}" }
        send(ServerboundRequestContinuation)
        completion.await()
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