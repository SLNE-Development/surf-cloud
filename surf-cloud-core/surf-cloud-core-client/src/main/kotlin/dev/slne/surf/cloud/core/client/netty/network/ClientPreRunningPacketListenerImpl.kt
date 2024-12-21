package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.netty.network.AbstractStatusUpdater.State
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ClientPreRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ClientboundPreRunningFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerboundPreRunningAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.ServerboundReadyToRunPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import kotlinx.coroutines.CompletableDeferred

class ClientPreRunningPacketListenerImpl(
    val client: ClientNettyClientImpl,
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val statusUpdater: AbstractStatusUpdater,
    val awaitFinishPreRunning: CompletableDeferred<Unit>
) : ClientCommonPacketListenerImpl(connection), ClientPreRunningPacketListener {

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

    suspend fun proceedToRunningState() {
        check(statusUpdater.getState() == State.PRE_RUNNING) { "Cannot proceed to running state from ${statusUpdater.getState()}" }

        val listener = ClientRunningPacketListenerImpl(connection, platformExtension)
        connection.setupInboundProtocol(
            RunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundReadyToRunPacket)
        connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)
        client.initListener(listener)
        statusUpdater.switchState(State.CONNECTED)
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        throw FatalSurfError {
            simpleErrorMessage("Client disconnected from the server before the connection was fully established.")
            detailedErrorMessage("Reason: ${details.reason}")
            possibleSolution("Try restarting the client.")
        }
    }
}