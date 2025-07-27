package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.synchronize
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress
import kotlin.time.Duration

abstract class CommonNettyClientImpl(
    serverId: Long,
    override val serverCategory: String,
    override val serverName: String
) : NettyClient {
    protected var internalServerId: Long = serverId

    override val serverId: Long get() = internalServerId

    private val packetQueue by lazy { mutableObject2ObjectMapOf<NettyPacket, CompletableDeferred<Boolean>?>().synchronize() }

    private var _connection: ConnectionImpl? = null
        set(value) {
            field = value

            if (value != null) {
                synchronized(packetQueue) {
                    packetQueue.forEach { (packet, deferred) ->
                        if (deferred != null) {
                            value.sendWithIndication(packet, deferred = deferred)
                        } else {
                            value.send(packet)
                        }
                    }
                    packetQueue.clear()
                }
            }
        }

    override val connection get() = _connection ?: error("connection not yet set")

    abstract val playAddress: InetSocketAddress
    val displayName get() = "${serverCategory}/${serverId} $serverName (${_connection?.getLoggableAddress()})"

    override fun fireAndForget(packet: NettyPacket) {
        val connection = _connection
        if (connection == null) {
            packetQueue[packet] = null
        } else {
            connection.send(packet)
        }
    }

    override suspend fun fire(packet: NettyPacket, convertExceptions: Boolean): Boolean {
        val connection = _connection
        if (connection == null) {
            val result = runCatching {
                val deferred = CompletableDeferred<Boolean>()
                packetQueue[packet] = deferred
                deferred.await()
            }

            if (convertExceptions) {
                return result.getOrDefault(false)
            }

            return result.getOrThrow()
        } else {
            return connection.sendWithIndication(packet, convertExceptions)
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
    }
}