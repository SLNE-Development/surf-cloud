package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val KEEP_ALIVE_TIME = KeepAliveTime(15.seconds)
private val KEEP_ALIVE_TIMEOUT = KeepAliveTime(30.seconds)

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

    private var keepAliveTime = KeepAliveTime.now()
    private var keepAlivePending = false
    private var keepAliveChallenge = KeepAliveTime(0)
    var processedDisconnect = false
        private set
    private var closedListenerTime: Long = 0
    private var closed = false

    @Volatile
    var suspendFlushingOnServerThread = false
        private set

    var latency = 0
        private set

    var keepConnectionAlive = true

    @OverridingMethodsMustInvokeSuper
    override suspend fun tick0() {
        if (keepConnectionAlive) {
            keepConnectionAlive()
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

    private fun handleBroadcastPacket(packets: List<NettyPacket>) {
        if (packets.isEmpty()) return
        val packet = if (packets.size == 1) packets.first() else ClientboundBundlePacket(packets)
        broadcast(packet)
    }

    override suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket) {
        if (keepAlivePending && packet.keepAliveId == keepAliveChallenge.time) {
            val elapsedTime = KeepAliveTime.now() - keepAliveTime

            this.latency = ((latency * 3 + elapsedTime) / 4).toInt()
            this.keepAlivePending = false
        } else {
            disconnect("Invalid keep alive")
        }
    }


    override fun handlePingRequest(packet: ServerboundPingRequestPacket) {
        send(ClientboundPongResponsePacket(packet.time))
    }

    private suspend fun keepConnectionAlive() {
        val currentTime = KeepAliveTime.now()
        val elapsedTime = currentTime - keepAliveTime

        if (KEEP_ALIVE_TIME.isExpired(elapsedTime)) {
            if (keepAlivePending && KEEP_ALIVE_TIMEOUT.isExpired(elapsedTime)) {
                disconnect("Timed out")
            } else if (checkIfClosed(currentTime)) {
                keepAlivePending = true
                keepAliveTime = currentTime
                keepAliveChallenge = currentTime
                send(ClientboundKeepAlivePacket(keepAliveChallenge.time))
            }
        }
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
    suspend fun disconnect(reason: String) {
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
            .log("${client.displayName} lost connection: ${details.reason}")
    }

    private suspend fun checkIfClosed(time: KeepAliveTime): Boolean {
        if (closed) {
            if (KEEP_ALIVE_TIME.isExpired(time - closedListenerTime)) {
                disconnect("Timed out")
            }

            return false
        }

        return true
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

/**
 * Inline value class representing a keep-alive timestamp.
 *
 * @property time the timestamp in milliseconds.
 */
@JvmInline
value class KeepAliveTime(val time: Long) {

    /**
     * Checks if the given elapsed time has exceeded this time.
     *
     * @param elapsedTime the elapsed time to check.
     * @return `true` if expired, `false` otherwise.
     */
    fun isExpired(elapsedTime: KeepAliveTime) = elapsedTime >= this

    operator fun compareTo(other: KeepAliveTime) = time.compareTo(other.time)
    operator fun minus(other: KeepAliveTime) = KeepAliveTime(time - other.time)
    operator fun minus(other: Long) = KeepAliveTime(time - other)

    companion object {
        /**
         * Returns the current time as a [KeepAliveTime].
         */
        fun now() = KeepAliveTime(System.currentTimeMillis())
    }
}

/**
 * Constructs a [KeepAliveTime] from a [Duration].
 *
 * @param duration the duration to convert.
 * @return the resulting [KeepAliveTime].
 */
fun KeepAliveTime(duration: Duration) = KeepAliveTime(duration.inWholeMilliseconds)

/**
 * Adds a [KeepAliveTime] to an integer value.
 *
 * @receiver the integer value.
 * @param time the [KeepAliveTime] to add.
 * @return the resulting sum.
 */
operator fun Int.plus(time: KeepAliveTime) = this + time.time