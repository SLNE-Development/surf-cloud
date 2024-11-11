package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import kotlinx.coroutines.CompletableDeferred

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

    abstract fun broadcast(packets: List<NettyPacket>)

    protected fun initConnection(connection: ConnectionImpl) {
        _connection = connection
    }
}