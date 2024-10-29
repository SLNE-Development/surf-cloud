package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val KEEP_ALIVE_TIME = KeepAliveTime(15.seconds)
private val KEEP_ALIVE_TIMEOUT = KeepAliveTime(30.seconds)

abstract class ServerCommonPacketListener(val connection: Connection) {

    private var keepAliveTime = KeepAliveTime.now()
    private var keepAlivePending = false
    private var keepAliveChallenge: KeepAliveTime = KeepAliveTime(0)
    private var processedDisconnect = false
    private var closedListenerTime: Long = 0

    @Volatile
    private var suspendFlushingOnServerThread = false

    private var closed = false

    private fun keepConnectionAlive() {
        val currentTime = KeepAliveTime.now()
        val elapsedTime = currentTime - keepAliveTime

        if (KEEP_ALIVE_TIME.isExpired(elapsedTime)) {
            if (keepAlivePending && KEEP_ALIVE_TIMEOUT.isExpired(elapsedTime)) {
                disconnect("Timed out")
            } else if (checkIfClosed(currentTime)) {
                keepAlivePending = true
                keepAliveTime = currentTime
                keepAliveChallenge = currentTime
            }
        }
    }

    private fun checkIfClosed(time: KeepAliveTime): Boolean {
        if (closed) {
            if (KEEP_ALIVE_TIME.isExpired(time - closedListenerTime)) {
                disconnect("Timed out")
            }

            return false
        }

        return true
    }

    fun disconnect(reason: String) {
        if (processedDisconnect) return
        processedDisconnect = true

        onDisconnect(reason)
        connection.setReadOnly()
        connection.disconnect(reason)

        server.schedule { connection.handleDisconnection() }
    }

    open fun onDisconnect(reason: String) {

    }

    fun send(packet: NettyPacket) {
        if (processedDisconnect) return

        val flush = !suspendFlushingOnServerThread
        connection.send(packet, flush)
    }

    fun suspendFlushing() {
        this.suspendFlushingOnServerThread = true
    }

    fun resumeFlushing() {
        this.suspendFlushingOnServerThread = false
        connection.flushChannel()
    }
}

@JvmInline
value class KeepAliveTime(private val time: Long) {
    constructor(duration: Duration) : this(duration.inWholeMilliseconds)

    fun isExpired(elapsedTime: KeepAliveTime) = elapsedTime >= this

    operator fun compareTo(other: KeepAliveTime) = time.compareTo(other.time)
    operator fun minus(other: KeepAliveTime) = KeepAliveTime(time - other.time)
    operator fun minus(other: Long) = KeepAliveTime(time - other)

    companion object {
        fun now() = KeepAliveTime(System.currentTimeMillis())
    }
}