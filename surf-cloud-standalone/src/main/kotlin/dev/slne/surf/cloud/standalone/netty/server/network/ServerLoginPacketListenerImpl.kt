package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

private const val MAX_LOGIN_TIME = 30

class ServerLoginPacketListenerImpl(val server: NettyServerImpl, val connection: ConnectionImpl) :
    CommonTickablePacketListener(), ServerLoginPacketListener {

    private val log = logger()
    private var client: ServerClientImpl? = null

    @Volatile
    var state: State = State.HELLO
        private set

    private var seconds = 0

    override suspend fun tick0() {
        if (state == State.VERIFYING) {
            if (connection.connected) {
                verifyLoginAndFinishConnectionSetup()
            }
        }

        if (seconds++ == MAX_LOGIN_TIME) {
            disconnect("Took too long to log in")
        }
    }

    override fun handleLoginStart(packet: ServerboundLoginStartPacket) {
        check(state == State.HELLO) { "Unexpected login start packet" }
        this.client = ServerClientImpl(server, packet.serverId, packet.serverCategory, packet.serverName)
        startClientVerification()
    }

    private fun startClientVerification() {
        state = State.VERIFYING
    }


    private fun verifyLoginAndFinishConnectionSetup() {
        finishLoginAndWaitForClient()
    }

    private fun finishLoginAndWaitForClient() {
        state = State.PROTOCOL_SWITCHING
        connection.send(ClientboundLoginFinishedPacket)
    }


    override suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket) {
        check(state == State.PROTOCOL_SWITCHING) { "Unexpected login acknowledgement packet" }
        val client = client ?: error("Client not yet set")

        connection.setupOutboundProtocol(RunningProtocols.CLIENTBOUND)
        val listener = ServerRunningPacketListenerImpl(server, client, connection)
        connection.setupInboundProtocol(
            RunningProtocols.SERVERBOUND,
            listener
        )
        client.initListener(listener)
        state = State.ACCEPTED
        server.registerClient(client)
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("${client?.displayName} lost connection: ${details.reason}")
    }

    fun disconnect(reason: String) {
        disconnect(DisconnectionDetails(reason))
    }

    fun disconnect(details: DisconnectionDetails) {
        runCatching {
            log.atInfo().log("Disconnecting ${client?.displayName}: ${details.reason}")
            connection.send(ClientboundLoginDisconnectPacket(details))
            connection.disconnect(details)
        }.onFailure {
            log.atSevere().withCause(it).log("Failed to disconnect ${client?.displayName}")
        }
    }

    enum class State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        VERIFYING,
        PROTOCOL_SWITCHING,
        ACCEPTED
    }
}