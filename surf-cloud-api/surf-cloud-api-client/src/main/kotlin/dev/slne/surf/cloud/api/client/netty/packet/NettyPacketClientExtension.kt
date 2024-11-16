package dev.slne.surf.cloud.api.client.netty.packet

import dev.slne.surf.cloud.api.client.netty.nettyManager
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

suspend fun NettyPacket.fire(convertExceptions: Boolean = true) {
    nettyManager.client.fire(this, convertExceptions)
}

fun NettyPacket.fireAndForget() {
    nettyManager.client.fireAndForget(this)
}