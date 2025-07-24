package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.surfapi.core.api.util.logger

private const val MAX_LOGIN_TIME = 30

class ServerLoginPacketListenerImpl(val server: NettyServerImpl, val connection: ConnectionImpl) :
    CommonTickablePacketListener(), ServerLoginPacketListener {

    private val log = logger()
    private var client: ServerClientImpl? = null
    private var proxy: Boolean = false


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
            disconnect(DisconnectReason.TOOK_TOO_LONG)
        }
    }

    override fun handleLoginStart(packet: ServerboundLoginStartPacket) {
        check(state == State.HELLO) { "Unexpected login start packet" }

        this.client = ServerClientImpl.fromPacket(server, packet)
        this.proxy = packet.proxy

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

        client!!.initConnection(connection)
        connection.setupOutboundProtocol(PreRunningProtocols.CLIENTBOUND)
        val listener = ServerPreRunningPacketListenerImpl(server, connection, client!!, proxy)
        connection.setupInboundProtocol(PreRunningProtocols.SERVERBOUND, listener)
        state = State.ACCEPTED
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("${client?.displayName} lost connection: ${details.buildMessage()}")
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }

    fun disconnect(reason: DisconnectReason) {
        disconnect(DisconnectionDetails(reason))
    }

    fun disconnect(details: DisconnectionDetails) {
        runCatching {
            log.atInfo().log("Disconnecting ${client?.displayName}: ${details.buildMessage()}")
            connection.send(ClientboundLoginDisconnectPacket(details))
            connection.disconnect(details)
        }.onFailure {
            log.atSevere().withCause(it).log("Failed to disconnect ${client?.displayName}")
        }
    }

    enum class State {
        HELLO,
        NEGOTIATING,
        VERIFYING,
        PROTOCOL_SWITCHING,
        ACCEPTED
    }
}