package dev.slne.surf.cloud.core.netty.client.network

import dev.slne.surf.cloud.core.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.login.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.netty.network.protocol.login.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.netty.network.protocol.running.RunningProtocols
import java.util.concurrent.atomic.AtomicReference

typealias StatusUpdate = (String) -> Unit

class ClientHandshakePacketListenerImpl(
    val client: ClientNettyClientImpl,
    val connection: Connection,
    val updateStatus: StatusUpdate
) : ClientLoginPacketListener {
    private val state = AtomicReference(State.CONNECTING)

    override suspend fun handleLoginFinished(packet: ClientboundLoginFinishedPacket) {
        switchState(State.JOINING)
        val listener = ClientRunningPacketListenerImpl(connection)
        connection.setupInboundProtocol(
            RunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundLoginAcknowledgedPacket)
        connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)
        client.initListener(listener)
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
        JOINING("Joining world...", setOf(ENCRYPTING, CONNECTING));
    }
}