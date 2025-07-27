package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ClientIntent
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ServerHandshakePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.serverbound.PROTOCOL_VERSION
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.serverbound.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.InitializeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import java.net.InetSocketAddress

class ServerHandshakePacketListenerImpl(
    val server: NettyServerImpl,
    val connection: ConnectionImpl
) :
    ServerHandshakePacketListener {
    override suspend fun handleHandshake(packet: ServerboundHandshakePacket) {
        connection.hostname = "${packet.hostName}:${packet.port}"

        when (packet.intention) {
            ClientIntent.INITIALIZE -> initialize(packet)
            ClientIntent.LOGIN -> beginLogin(packet)
            ClientIntent.STATUS -> error("Status intention is not supported")
        }

        connection.virtualHost = prepareVirtualHost(packet.hostName, packet.port)
    }

    private suspend fun beginLogin(packet: ServerboundHandshakePacket) {
        connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND)

        if (packet.protocolVersion != PROTOCOL_VERSION) {
            val reason =
                if (packet.protocolVersion > PROTOCOL_VERSION) DisconnectReason.OUTDATED_SERVER else DisconnectReason.OUTDATED_CLIENT
            connection.send(ClientboundLoginDisconnectPacket(reason))
            connection.disconnect(reason)

            return
        }

        connection.setupInboundProtocol(
            LoginProtocols.SERVERBOUND,
            ServerLoginPacketListenerImpl(server, connection)
        )
    }

    private suspend fun initialize(unused: ServerboundHandshakePacket) {
        connection.setupOutboundProtocol(InitializeProtocols.CLIENTBOUND)
        connection.setupInboundProtocol(
            InitializeProtocols.SERVERBOUND,
            ServerInitializePacketListenerImpl(connection)
        )
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) = Unit
    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }

    private fun prepareVirtualHost(host: String, port: Int): InetSocketAddress {
        var len = host.length

        // FML appends a marker to the host to recognize FML clients (\0FML\0)
        val pos = host.indexOf('\u0000')
        if (pos >= 0) {
            len = pos
        }

        // When clients connect with a SRV record, their host contains a trailing '.'
        if (len > 0 && host[len - 1] == '.') {
            len--
        }

        return InetSocketAddress.createUnresolved(host.substring(0, len), port)
    }
}