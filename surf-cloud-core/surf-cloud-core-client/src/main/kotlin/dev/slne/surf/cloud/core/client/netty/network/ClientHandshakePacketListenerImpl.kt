package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import java.util.concurrent.atomic.AtomicReference

typealias StatusUpdate = (String) -> Unit

class ClientHandshakePacketListenerImpl(
    val client: ClientNettyClientImpl,
    val connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val updateStatus: StatusUpdate
) : ClientLoginPacketListener {
    private val state = AtomicReference(State.CONNECTING)

    override suspend fun handleLoginFinished(packet: ClientboundLoginFinishedPacket) {
        switchState(State.PREPARE_CONNECTION)

        val listener = ClientRunningPacketListenerImpl(connection, platformExtension)
        connection.setupInboundProtocol(
            RunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundLoginAcknowledgedPacket)
        connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)
        client.initListener(listener)

        switchState(State.CONNECTED)
    }

    override fun onDisconnect(details: DisconnectionDetails) {

    }

    private fun switchState(newState: State) {
        val updatedState = this.state.updateAndGet {
            check(newState.fromStates.contains(it)) { "Tried to switch to $newState from $it, but expected one of ${newState.fromStates}" }
            newState
        }
        this.updateStatus(updatedState.stateIndication)
    }

    enum class State(val stateIndication: String, val fromStates: Set<State> = setOf()) {
        CONNECTING("Connecting to the server..."),
        AUTHORIZING("Logging in...", setOf(CONNECTING)),
        ENCRYPTING("Encrypting...", setOf(AUTHORIZING)),
        PREPARE_CONNECTION("Preparing connection...", setOf(ENCRYPTING, CONNECTING)),
        CONNECTED("Connected!", setOf(PREPARE_CONNECTION))
    }
}