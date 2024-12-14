package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols
import dev.slne.surf.cloud.core.common.util.encryption.Crypt
import kotlinx.coroutines.CompletableDeferred
import javax.crypto.SecretKey


class ClientHandshakePacketListenerImpl(
    val client: ClientNettyClientImpl,
    val connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    updateStatus: StatusUpdate,
    val awaitFinishPreRunning: CompletableDeferred<Unit>
) : AbstractStatusUpdater(State.CONNECTING, updateStatus), ClientLoginPacketListener {

    override suspend fun handleLoginFinished(packet: ClientboundLoginFinishedPacket) {
        switchState(State.PREPARE_CONNECTION)

        val listener = ClientPreRunningPacketListenerImpl(
            client,
            connection,
            platformExtension,
            getState(),
            updateStatus,
            awaitFinishPreRunning
        )
        connection.setupInboundProtocol(
            PreRunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundLoginAcknowledgedPacket)
        connection.setupOutboundProtocol(PreRunningProtocols.SERVERBOUND)

        switchState(State.PREPARE_CONNECTION)
    }

    override suspend fun handleKey(packet: ClientboundKeyPacket) {
        switchState(State.AUTHORIZING)

        val secretKey = Crypt.generateSecretKey()
        val publicKey = packet.decryptPublicKey()
        val responsePacket = ServerboundKeyPacket(secretKey, publicKey, packet.challenge)

        setEncryption(responsePacket, secretKey)
    }

    private suspend fun setEncryption(
        responsePacket: ServerboundKeyPacket,
        secretKey: SecretKey,
    ) {
        switchState(State.ENCRYPTING)
        connection.send(responsePacket)
        connection.setupEncryption(secretKey)
    }

    override fun onDisconnect(details: DisconnectionDetails) {

    }
}