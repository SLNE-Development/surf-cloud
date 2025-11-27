package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

abstract class CommonNettyClientImpl(
    override val serverCategory: String,
    override val serverName: String
) : NettyClient {
    @Volatile
    private var _connection: ConnectionImpl? = null
    private val packetQueue = ConcurrentLinkedQueue<QueuedPacket>()

    override val connection get() = _connection ?: error("connection not yet set")

    abstract val playAddress: InetSocketAddress
    val displayName get() = "$serverName/$serverCategory (${_connection?.getLoggableAddress()})"

    override val server: CommonCloudServer?
        get() = CloudServer[serverName]

    override fun fireAndForget(packet: NettyPacket) {
        val connection = _connection
        if (connection != null) {
            connection.send(packet)
            return
        }

        packetQueue.add(QueuedPacket(packet, null))

        val connectionNow = _connection
        if (connectionNow != null) {
            drainQueue(connectionNow)
        }
    }

    override suspend fun fire(packet: NettyPacket, convertExceptions: Boolean): Boolean {
        val connection = _connection
        if (connection != null) {
            return connection.sendWithIndication(packet, convertExceptions)
        }

        val deferred = CompletableDeferred<Boolean>()
        packetQueue.add(QueuedPacket(packet, deferred))

        val connectionNow = _connection
        if (connectionNow != null) {
            drainQueue(connectionNow)
        }

        val result = runCatching { deferred.await() }

        return if (convertExceptions) {
            result.getOrDefault(false)
        } else {
            result.getOrThrow()
        }
    }

    override suspend fun <P : ResponseNettyPacket> fireAndAwait(
        packet: RespondingNettyPacket<P>,
        timeout: Duration
    ): P? {
        return packet.fireAndAwait(connection, timeout)
    }

    abstract fun broadcast(packets: List<NettyPacket>)

    fun initConnection(connection: ConnectionImpl) {
        check(_connection == null) { "Connection already set" }
        _connection = connection
        drainQueue(connection)
    }


    private fun drainQueue(connection: ConnectionImpl) {
        while (true) {
            val queued = packetQueue.poll() ?: break
            val (packet, deferred) = queued
            if (deferred != null) {
                connection.sendWithIndication(packet, deferred = deferred)
            } else {
                connection.send(packet)
            }
        }
    }

    private data class QueuedPacket(
        val packet: NettyPacket,
        val deferred: CompletableDeferred<Boolean>?
    )
}