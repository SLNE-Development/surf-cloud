package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.coroutines.NettyConnectionScope
import dev.slne.surf.cloud.core.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.ServerboundBroadcastPacket
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


private val KEEP_ALIVE_TIME = KeepAliveTime(15.seconds)
private val KEEP_ALIVE_TIMEOUT = KeepAliveTime(30.seconds)

class ServerRunningPacketListenerImpl(
    val server: NettyServerImpl,
    val client: ServerClientImpl,
    val connection: Connection
) :
    CommonTickablePacketListener(), RunningServerPacketListener {
    private val log = logger()

    private var keepAliveTime = KeepAliveTime.now()
    private var keepAlivePending = false
    private var keepAliveChallenge: KeepAliveTime = KeepAliveTime(0)
    private var processedDisconnect = false
    private var closedListenerTime: Long = 0
    var latency = 0
        private set

    @Volatile
    private var suspendFlushingOnServerThread = false

    private var closed = false

    override fun handleBroadcastPacket(packet: ServerboundBroadcastPacket) {
        for (receiver in server.connection.connections) {
            receiver.send(packet.packet)
        }
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

    fun handlePongPacket(server: ServerboundPongPacket) {
        TODO("Not yet implemented")
    }

    override fun handlePingRequest(packet: ServerboundPingRequestPacket) {
        connection.send(ClientboundPongResponsePacket(packet.time))
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val (proxiedSource, finalPacket) = when (packet) {
            is ProxiedNettyPacket -> packet.source to packet.packet
            else -> null to packet
        }
        val info = NettyPacketInfo(this, proxiedSource)

        for (listener in listeners) {
            NettyListenerScope.launch {
                try {
                    listener.handle(finalPacket, info)
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .atMostEvery(5, TimeUnit.SECONDS)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener::class.simpleName,
                            finalPacket::class.simpleName
                        )
                }
            }
        }
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return
        processedDisconnect = true

        log.atInfo()
            .log("${client.serverCategory}/${client.serverId} (${client.host}) lost connection: ${details.reason}")
    }

    private fun close() {
        if (!closed) {
            closedListenerTime = System.currentTimeMillis()
            closed = true
        }
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

    private suspend fun checkIfClosed(time: KeepAliveTime): Boolean {
        if (closed) {
            if (KEEP_ALIVE_TIME.isExpired(time - closedListenerTime)) {
                disconnect("Timed out")
            }

            return false
        }

        return true
    }

    fun suspendFlushing() {
        this.suspendFlushingOnServerThread = true
    }

    fun resumeFlushing() {
        this.suspendFlushingOnServerThread = false
        connection.flushChannel()
    }

    fun send(packet: NettyPacket) {
        if (processedDisconnect) return

        if (packet.terminal) {
            close()
        }

        val flush = !suspendFlushingOnServerThread
        connection.send(packet, flush)
    }

    suspend fun disconnect(reason: String) {
        disconnect(DisconnectionDetails(reason))
    }

    suspend fun disconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return

        NettyConnectionScope.launch {
            connection.sendWithIndication(ClientboundDisconnectPacket(details))
            connection.disconnect(details)
        }

        onDisconnect(details)
        connection.setReadOnly()

        schedule { connection.handleDisconnection() }
    }

    override suspend fun tick0() {
        keepConnectionAlive()
    }
}

@JvmInline
value class KeepAliveTime(val time: Long) {

    fun isExpired(elapsedTime: KeepAliveTime) = elapsedTime >= this

    operator fun compareTo(other: KeepAliveTime) = time.compareTo(other.time)
    operator fun minus(other: KeepAliveTime) = KeepAliveTime(time - other.time)
    operator fun minus(other: Long) = KeepAliveTime(time - other)

    companion object {
        fun now() = KeepAliveTime(System.currentTimeMillis())
    }
}

fun KeepAliveTime(duration: Duration) = KeepAliveTime(duration.inWholeMilliseconds)

operator fun Int.plus(time: KeepAliveTime) = this + time.time