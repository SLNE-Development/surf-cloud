package dev.slne.surf.cloud.core.netty

import dev.slne.surf.cloud.api.netty.NettyClient
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.util.synchronize
import dev.slne.surf.cloud.core.netty.network.Connection
import kotlinx.coroutines.CompletableDeferred

abstract class CommonNettyClientImpl(
    serverId: Long,
    serverCategory: String
) : NettyClient {
    protected var internalServerId: Long = serverId
    private var internalServerCategory: String = serverCategory

    override val serverId: Long get() = internalServerId
    override val serverCategory: String get() = internalServerCategory

    private val packetQueue by lazy { mutableObject2ObjectMapOf<NettyPacket, CompletableDeferred<Boolean>?>().synchronize() }

    private var _connection: Connection? = null
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

    val connection get() = _connection ?: error("connection not yet set")

    override val receiving get() = connection.receiving
    override val sending get() = connection.sending
    override val receivedPackets get() = _connection?.receivedPackets ?: 0
    override val sentPackets get() = _connection?.sentPackets ?: 0
    override val averageReceivedPackets get() = _connection?.averageReceivedPackets ?: 0f
    override val averageSentPackets get() = _connection?.averageSentPackets ?: 0f
    override val host get() = connection.hostname
    override val virtualHost get() = connection.virtualHost

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

    override suspend fun <P : NettyPacket> fireAndAwait(packet: RespondingNettyPacket<P>): P? {
        TODO("Not yet implemented")
    }

    protected fun initConnection(connection: Connection) {
        _connection = connection
    }
}