package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBroadcastPacket
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import javax.annotation.OverridingMethodsMustInvokeSuper


/**
 * Implementation of the server-side common packet listener that manages keep-alive checks,
 * packet handling, and connection disconnection.
 *
 * @property server the server instance managing the connection.
 * @property client the client instance associated with this connection.
 * @property connection the connection implementation for this listener.
 */
abstract class ServerCommonPacketListenerImpl(
    val server: NettyServerImpl,
    val client: ServerClientImpl,
    val connection: ConnectionImpl
) : CommonTickablePacketListener(), ServerCommonPacketListener {
    private val log = logger()

    private val keepAlive = KeepAliveHandler(
        connection,
        { disconnect(it) },
        { processedDisconnect },
        { closed },
        { closedListenerTime }
    )

    var processedDisconnect = false
        private set
    private var closedListenerTime: Long = 0
    private var closed = false

    @Volatile
    var suspendFlushingOnServerThread = false
        private set

    val latency get() = connection.latency
    var keepConnectionAlive = true

    @OverridingMethodsMustInvokeSuper
    override suspend fun tick0() {
        if (keepConnectionAlive) {
            keepAlive.keepConnectionAlive()
        }
    }

    override fun handleBundlePacket(packet: ServerboundBundlePacket) {
        val broadcastIdentifier = packet.subPackets.firstOrNull() as? ServerboundBroadcastPacket

        if (broadcastIdentifier != null) {
            handleBroadcastPacket(packet.subPackets.drop(1))
            return
        }

        ConnectionManagementScope.launch {
            for (subPacket in packet.subPackets) {
                connection.handlePacket(subPacket)
            }
        }
    }

    override suspend fun handleKeepAlivePacket(packet: KeepAlivePacket) {
        packet.respond(packet.keepAliveId)
    }

    private fun handleBroadcastPacket(packets: List<NettyPacket>) {
        if (packets.isEmpty()) return
        val packet = if (packets.size == 1) packets.first() else ClientboundBundlePacket(packets)
        broadcast(packet)
    }

    override fun handlePingRequest(packet: ServerboundPingRequestPacket) {
        send(ClientboundPongResponsePacket(packet.time))
    }

    /**
     * Sends a packet to the client.
     *
     * @param packet the packet to send.
     */
    fun send(packet: NettyPacket) {
        if (processedDisconnect) return

        if (packet.terminal) {
            close()
        }

        val flush = !suspendFlushingOnServerThread
        connection.send(packet, flush)
    }

    /**
     * Suspends automatic flushing of packets on the server thread.
     */
    fun suspendFlushing() {
        this.suspendFlushingOnServerThread = true
    }

    /**
     * Resumes automatic flushing of packets on the server thread.
     */
    fun resumeFlushing() {
        this.suspendFlushingOnServerThread = false
        connection.flushChannel()
    }

    /**
     * Disconnects the client with the specified reason.
     *
     * @param reason the reason for the disconnection.
     */
    suspend fun disconnect(reason: DisconnectReason) {
        disconnect(DisconnectionDetails(reason))
    }

    /**
     * Disconnects the client with the specified disconnection details.
     *
     * @param details the details of the disconnection.
     */
    suspend fun disconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return

        ConnectionManagementScope.launch {
            connection.sendWithIndication(ClientboundDisconnectPacket(details))
            connection.disconnect(details)
        }

        onDisconnect(details)
        connection.setReadOnly()

        schedule { connection.handleDisconnection() }
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return
        processedDisconnect = true

        server.unregisterClient(client)

        log.atInfo()
            .log("${client.displayName} lost connection: ${details.buildMessage()}")
    }

    protected fun close() {
        if (!closed) {
            closedListenerTime = System.currentTimeMillis()
            closed = true
        }
    }

    /**
     * Broadcasts a packet to all connected clients.
     *
     * @param packet the packet to broadcast.
     */
    fun broadcast(packet: NettyPacket) {
        if (processedDisconnect) return

        if (packet.terminal) {
            close()
        }

        val flush = !suspendFlushingOnServerThread
        server.connection.broadcast(packet, flush)
    }
}

