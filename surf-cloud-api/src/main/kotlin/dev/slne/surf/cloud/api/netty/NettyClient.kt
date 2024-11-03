package dev.slne.surf.cloud.api.netty

import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.RespondingNettyPacket
import java.net.InetSocketAddress

interface NettyClient {
    val serverId: Long
    val serverCategory: String
    val receiving: PacketFlow
    val sending: PacketFlow
    val receivedPackets: Int
    val sentPackets: Int
    val averageReceivedPackets: Float
    val averageSentPackets: Float

    /**
     * The hostname of the connection (address:port)
     */
    val host: String
    val virtualHost: InetSocketAddress

    fun fireAndForget(packet: NettyPacket)
    suspend fun fire(packet: NettyPacket, convertExceptions: Boolean = true): Boolean
    suspend fun <P : NettyPacket> fireAndAwait(packet: RespondingNettyPacket<P>): P?
}