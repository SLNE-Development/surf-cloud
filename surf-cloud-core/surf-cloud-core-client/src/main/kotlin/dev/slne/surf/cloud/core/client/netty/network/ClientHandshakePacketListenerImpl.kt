package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.PreRunningProtocols


class ClientHandshakePacketListenerImpl(
    val client: ClientNettyClientImpl,
    val connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    updateStatus: StatusUpdate,
) : AbstractStatusUpdater(State.CONNECTING, updateStatus), ClientLoginPacketListener {

    override fun handleLoginFinished(packet: ClientboundLoginFinishedPacket) {
        switchState(State.PREPARE_CONNECTION)

        client.initConnection(connection)
        val listener = ClientPreRunningPacketListenerImpl(
            client,
            connection,
            platformExtension,
            this@ClientHandshakePacketListenerImpl
        )
        client.connectionManager.currentPacketLister = listener
        connection.setupInboundProtocol(
            PreRunningProtocols.CLIENTBOUND,
            listener
        ).syncUninterruptibly()
        connection.send(ServerboundLoginAcknowledgedPacket)
        connection.setupOutboundProtocol(PreRunningProtocols.SERVERBOUND).syncUninterruptibly()
        switchState(State.PRE_PRE_RUNNING)
    }

    override fun handleDisconnect(packet: ClientboundLoginDisconnectPacket) {
        connection.disconnect(packet.details)
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        if (details.reason.shouldReconnect) {
            client.connectionManager.tryStartScheduleReconnect(false, details.createException())
        }
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}