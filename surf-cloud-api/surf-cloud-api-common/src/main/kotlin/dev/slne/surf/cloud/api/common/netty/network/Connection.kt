package dev.slne.surf.cloud.api.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress

/**
 * Interface representing a connection in the Netty framework, handling packet transmission and network metrics.
 */
interface Connection {
    val receivedPackets: Int
    val sentPackets: Int
    val averageReceivedPackets: Float
    val averageSentPackets: Float

    val hostname: String
    val virtualHost: InetSocketAddress

    /**
     * Sends a packet to the connection.
     *
     * @param packet The [NettyPacket] to send.
     * @param flush Whether to flush the packet immediately. Defaults to `true`.
     */
    fun send(packet: NettyPacket, flush: Boolean = true)

    /**
     * Sends a packet with an indication of success, suspending until the operation completes.
     *
     * @param packet The [NettyPacket] to send.
     * @param flush Whether to flush the packet immediately. Defaults to `true`.
     * @param convertExceptions Whether to convert exceptions.
     * @return `true` if the packet was sent successfully; `false` otherwise.
     */
    suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        convertExceptions: Boolean = true
    ): Boolean

    /**
     * Sends a packet with a [CompletableDeferred] indication of success.
     *
     * @param packet The [NettyPacket] to send.
     * @param flush Whether to flush the packet immediately. Defaults to `true`.
     * @param deferred The [CompletableDeferred] to complete upon success or failure.
     */
    fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>
    )

    /**
     * Returns the address of the connection suitable for logging.
     *
     * @return The loggable address.
     */
    fun getLoggableAddress(): String
}