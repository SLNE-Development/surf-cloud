package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols


class ClientHandshakePacketListenerImpl(
    val client: ClientNettyClientImpl,
    val connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    updateStatus: StatusUpdate,
) : AbstractStatusUpdater(State.CONNECTING, updateStatus), ClientLoginPacketListener {

    override suspend fun handleLoginFinished(packet: ClientboundLoginFinishedPacket) {
        switchState(State.PREPARE_CONNECTION)

        val listener = ClientPreRunningPacketListenerImpl(
            client,
            connection,
            platformExtension,
            this,
        )
        connection.setupInboundProtocol(
            PreRunningProtocols.CLIENTBOUND,
            listener
        )
        connection.send(ServerboundLoginAcknowledgedPacket)
        connection.setupOutboundProtocol(PreRunningProtocols.SERVERBOUND)
        switchState(State.PRE_PRE_RUNNING)
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) = Unit
    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}