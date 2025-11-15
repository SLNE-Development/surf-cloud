package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.PacketListener

interface CommonPacketListener : PacketListener {

    fun handleKeepAlive(packet: KeepAlivePacket)

}