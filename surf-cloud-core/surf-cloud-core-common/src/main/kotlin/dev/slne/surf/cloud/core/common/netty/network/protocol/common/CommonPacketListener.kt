package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.PacketListener

interface CommonPacketListener : PacketListener {

    val client: CommonNettyClientImpl

    fun handleKeepAlive(packet: KeepAlivePacket)

}