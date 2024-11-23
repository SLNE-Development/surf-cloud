package dev.slne.surf.cloud.api.common.netty

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_TIMEOUT
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import kotlin.time.Duration

interface NettyClient {
    val serverId: Long
    val serverCategory: String
    val serverName: String

    val connection: Connection

    fun fireAndForget(packet: NettyPacket)
    suspend fun fire(packet: NettyPacket, convertExceptions: Boolean = true): Boolean
    suspend fun <P : ResponseNettyPacket> fireAndAwait(
        packet: RespondingNettyPacket<P>,
        timeout: Duration = DEFAULT_TIMEOUT
    ): P?
}