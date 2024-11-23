package dev.slne.surf.cloud.api.client.netty.packet

import dev.slne.surf.cloud.api.client.netty.nettyManager
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_TIMEOUT
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun NettyPacket.fire(convertExceptions: Boolean = true) {
    nettyManager.client.fire(this, convertExceptions)
}

fun NettyPacket.fireAndForget() {
    nettyManager.client.fireAndForget(this)
}

suspend fun <P : ResponseNettyPacket> RespondingNettyPacket<P>.fireAndAwait(timeout: Duration = DEFAULT_TIMEOUT): P? =
    fireAndAwait(nettyManager.client.connection, timeout)