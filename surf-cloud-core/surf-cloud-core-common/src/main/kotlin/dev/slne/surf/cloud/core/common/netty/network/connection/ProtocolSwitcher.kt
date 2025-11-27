package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ClientIntent
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.PROTOCOL_VERSION
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.InitializeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.ChannelFuture

internal class ProtocolSwitcher(private val connection: ConnectionImpl) {

    private fun validateListener(protocolInfo: ProtocolInfo<*>, listener: PacketListener) {
        check(listener.flow == connection.receiving) { "Trying to set listener for wrong side: connection is ${connection.receiving}, but listener is ${listener.flow}" }
        check(protocolInfo.id == listener.protocol) { "Listener protocol (${listener.protocol}) does not match requested one (${protocolInfo})" }
    }

    fun <T : PacketListener> setupInboundProtocol(
        newState: ProtocolInfo<T>,
        listener: T
    ): ChannelFuture {
        validateListener(newState, listener)
        check(newState.flow == connection.receiving) { "Invalid inbound protocol: ${newState.flow}" }

        connection._packetListener = listener

        var protocol = UnconfiguredPipelineHandler.setupInboundProtocol(newState)
        val bundlerInfo = newState.bundlerInfo

        if (bundlerInfo != null) {
            protocol += { ctx ->
                ctx.pipeline().addAfter(
                    HandlerNames.DECODER,
                    HandlerNames.BUNDLER,
                    PacketBundlePacker(bundlerInfo)
                )
            }
        }

        protocol += { connection.inboundProtocolInfo = newState }

        return connection.channel.writeAndFlush(protocol)
    }

    fun setupOutboundProtocol(newState: ProtocolInfo<*>): ChannelFuture {
        check(newState.flow == connection.sending) { "Invalid outbound protocol: ${newState.flow}" }

        var protocol = UnconfiguredPipelineHandler.setupOutboundProtocol(newState)
        val bundlerInfo = newState.bundlerInfo

        if (bundlerInfo != null) {
            protocol += {
                it.pipeline().addAfter(
                    HandlerNames.ENCODER,
                    HandlerNames.UNBUNDLER,
                    PacketBundleUnpacker(bundlerInfo)
                )
            }
        }

        val login = newState.id == ConnectionProtocol.LOGIN
        protocol += { connection.sendLoginDisconnect = login }
        protocol += { connection.outboundProtocolInfo = newState }

        return connection.channel.writeAndFlush(protocol)
    }


    fun initiateServerboundInitializeConnection(
        hostname: String,
        port: Int,
        listener: ClientInitializePacketListener
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            InitializeProtocols.SERVERBOUND,
            InitializeProtocols.CLIENTBOUND,
            listener,
            ClientIntent.INITIALIZE
        )
    }

    fun initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        listener: ClientLoginPacketListener
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            LoginProtocols.SERVERBOUND,
            LoginProtocols.CLIENTBOUND,
            listener,
            ClientIntent.LOGIN
        )
    }

    fun <S : ServerboundPacketListener, C : ClientboundPacketListener> initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        serverboundProtocolInfo: ProtocolInfo<S>,
        clientboundProtocolInfo: ProtocolInfo<C>,
        clientboundListener: C,
        initialze: Boolean
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            serverboundProtocolInfo,
            clientboundProtocolInfo,
            clientboundListener,
            if (initialze) ClientIntent.INITIALIZE else ClientIntent.LOGIN
        )
    }

    private fun <S : ServerboundPacketListener, C : ClientboundPacketListener> initiateServerboundConnection(
        hostname: String,
        port: Int,
        serverboundProtocolInfo: ProtocolInfo<S>,
        clientboundProtocolInfo: ProtocolInfo<C>,
        clientboundListener: C,
        intention: ClientIntent
    ) {
        check(serverboundProtocolInfo.id == clientboundProtocolInfo.id) { "Mismatched initial protocols" }

        connection.actionQueue.runOnceConnected {
            setupInboundProtocol(clientboundProtocolInfo, clientboundListener).syncUninterruptibly()
            sendPacketNow(
                ServerboundHandshakePacket(
                    PROTOCOL_VERSION,
                    hostname,
                    port,
                    intention
                ), flush = true
            )
            setupOutboundProtocol(serverboundProtocolInfo).syncUninterruptibly()
        }
    }

    companion object {
        private val log = logger()
    }
}