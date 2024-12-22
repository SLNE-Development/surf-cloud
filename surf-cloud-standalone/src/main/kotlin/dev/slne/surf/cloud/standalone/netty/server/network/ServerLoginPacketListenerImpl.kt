package dev.slne.surf.cloud.standalone.netty.server.network

import com.google.common.primitives.Ints
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.core.common.util.encryption.CryptException
import dev.slne.surf.cloud.core.common.util.random
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

private const val MAX_LOGIN_TIME = 30

class ServerLoginPacketListenerImpl(val server: NettyServerImpl, val connection: ConnectionImpl) :
    CommonTickablePacketListener(), ServerLoginPacketListener {

    private val log = logger()
    private var client: ServerClientImpl? = null
    private var proxy: Boolean = false

    private val challenge = Ints.toByteArray(random.nextInt())

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
        this.proxy = packet.proxy

        state = State.KEY
        val publicKey = server.keyPair.public.encoded

        connection.send(ClientboundKeyPacket(publicKey, challenge))
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

    override fun handleKey(packet: ServerboundKeyPacket) {
        check(state == State.KEY) { "Unexpected key packet" }

        try {
            val privateKey = server.keyPair.private
            check(packet.isChallengeValid(challenge, privateKey)) { "Protocol error: Invalid key" }

            val secretkey = packet.getSecretKey(privateKey)
            state = State.AUTHENTICATING
            connection.setupEncryption(secretkey)
        } catch (e: CryptException) {
            throw IllegalStateException("Failed to handle key packet", e)
        }

        startClientVerification()
    }

    override suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket) {
        check(state == State.PROTOCOL_SWITCHING) { "Unexpected login acknowledgement packet" }
        val client = client ?: error("Client not yet set")

        connection.setupOutboundProtocol(PreRunningProtocols.CLIENTBOUND)
        val listener = ServerPreRunningPacketListener(server, connection, client, proxy)
        connection.setupInboundProtocol(PreRunningProtocols.SERVERBOUND, listener)
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