package dev.slne.surf.cloud.standalone.netty.server.connection

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.PacketWire
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import kotlinx.coroutines.CompletableDeferred

class DirectPacketWire : PacketWire {
    lateinit var connectionImpl: ConnectionImpl

    override fun send(
        packet: NettyPacket,
        flush: Boolean
    ) {
        connectionImpl.internalSend(packet, flush)
    }

    override suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        convertExceptions: Boolean
    ): Boolean {
        val result = runCatching {
            val deferred = CompletableDeferred<Boolean>()
            connectionImpl.internalSend(packet, flush, deferred)
            deferred.await()
        }

        if (convertExceptions) {
            return result.getOrDefault(false)
        }

        return result.getOrThrow()
    }

    override fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>
    ) {
        connectionImpl.internalSend(packet, flush, deferred)
    }
}