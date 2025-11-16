package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPingPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundPongPacket
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch

abstract class ClientCommonPacketListenerImpl(
    val connection: ConnectionImpl,
) : CommonTickablePacketListener(), ClientCommonPacketListener {
    private val log = logger()

    private val keepAlive = KeepAliveHandler(
        connection,
        { disconnect(it) },
        { processedDisconnect },
        { closed },
        { closedListenerTime }
    )

    @Volatile
    var closed = false
        private set

    private var closedListenerTime: Long = 0
    var processedDisconnect = false
        private set

    override suspend fun tick0() {
        keepAlive.keepConnectionAlive()
    }

    override fun handleKeepAlive(packet: KeepAlivePacket) {
        packet.respond(packet.keepAliveId)
    }

    override fun handlePing(packet: ClientboundPingPacket) {
        send(ServerboundPongPacket(packet.pingId))
    }

    override fun handlePong(packet: ClientboundPongResponsePacket) {

    }

    override fun handleDisconnect(packet: ClientboundDisconnectPacket) {
        connection.disconnect(packet.details)
    }

    override fun handleBundlePacket(packet: ClientboundBundlePacket) {
        ConnectionManagementScope.launch {
            for (subPacket in packet.subPackets) {
                connection.handlePacket(subPacket)
            }
        }
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return
        processedDisconnect = true

        log.atInfo().log("Client disconnected with reason: ${details.buildMessage()}")

        bean<NettyManager>().blockPlayerConnections()
        if (details.reason.shouldRestart) {
            restart()
        } else {
            shutdown()
        }
    }

    fun send(packet: NettyPacket) {
        if (processedDisconnect) return
        if (packet.terminal) {
            close()
        }

        connection.send(packet)
    }

    fun close() {
        if (!closed) {
            closedListenerTime = System.currentTimeMillis()
            closed = true
        }
    }

    private suspend fun disconnect(reason: DisconnectReason) {
        disconnect(DisconnectionDetails(reason))
    }

    private suspend fun disconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return

        connection.disconnect(details)
        onDisconnect(details)
        connection.setReadOnly()
    }

    abstract fun restart()
    abstract fun shutdown()
}