package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ProxyServerAutoregistration
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

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
            disconnect("Took too long to log in")
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

        if (proxy && standaloneConfig.useSingleProxySetup && ProxyServerAutoregistration.hasProxy) {
            disconnect("Proxy already connected")
        }
    }

    private fun verifyLoginAndFinishConnectionSetup() {
        // compression would go here

        finishLoginAndWaitForClient()
    }

    private fun finishLoginAndWaitForClient() {
        state = State.PROTOCOL_SWITCHING
        connection.send(ClientboundLoginFinishedPacket)
    }

    override suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket) {
        check(state == State.PROTOCOL_SWITCHING) { "Unexpected login acknowledgement packet" }

        connection.setupOutboundProtocol(PreRunningProtocols.CLIENTBOUND)
        val listener = ServerPreRunningPacketListenerImpl(server, connection, client!!, proxy)
        connection.setupInboundProtocol(PreRunningProtocols.SERVERBOUND, listener)
        state = State.ACCEPTED
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("${client?.displayName} lost connection: ${details.reason}")
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
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
        NEGOTIATING,
        VERIFYING,
        PROTOCOL_SWITCHING,
        ACCEPTED
    }
}