package dev.slne.surf.cloud.api.common.netty

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_TIMEOUT
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import kotlin.time.Duration

/**
 * Interface for a Netty client, managing network communication and packet handling.
 */
interface NettyClient {
    val serverCategory: String
    val serverName: String

    val velocitySecret: ByteArray

    val connection: Connection

    val server: CommonCloudServer?

    /**
     * Sends a packet to the server without waiting for a response.
     *
     * @param packet The [NettyPacket] to send.
     */
    fun fireAndForget(packet: NettyPacket)

    /**
     * Sends a packet to the server and optionally converts exceptions.
     *
     * @param packet The [NettyPacket] to send.
     * @param convertExceptions Whether to convert exceptions.
     * @return `true` if the packet was successfully sent; `false` otherwise.
     */
    suspend fun fire(packet: NettyPacket, convertExceptions: Boolean = true): Boolean

    /**
     * Sends a responding packet and waits for a response within the specified timeout.
     *
     * @param packet The [RespondingNettyPacket] to send.
     * @param timeout The maximum time to wait for a response. Defaults to [DEFAULT_TIMEOUT].
     * @return The response packet, or `null` if the timeout elapsed.
     */
    suspend fun <P : ResponseNettyPacket> fireAndAwait(
        packet: RespondingNettyPacket<P>,
        timeout: Duration = DEFAULT_TIMEOUT
    ): P?
}