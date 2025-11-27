package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.coroutines.CompletableDeferred

interface PacketWire {
    fun send(packet: NettyPacket, flush: Boolean)

    suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        convertExceptions: Boolean
    ): Boolean

    fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>
    )
}