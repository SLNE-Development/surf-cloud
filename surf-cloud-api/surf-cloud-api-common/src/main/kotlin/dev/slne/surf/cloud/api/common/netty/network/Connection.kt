package dev.slne.surf.cloud.api.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

interface Connection {
    val receivedPackets: Int
    val sentPackets: Int
    val averageReceivedPackets: Float
    val averageSentPackets: Float

    val hostname: String
    val virtualHost: InetSocketAddress

    fun send(packet: NettyPacket) = send(packet, true)
    fun send(packet: NettyPacket, flush: Boolean)

    suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        convertExceptions: Boolean = true
    ): Boolean

    fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>
    )

    fun getLoggableAddress(): String
}