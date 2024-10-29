package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow

interface PacketListener {
    val flow: PacketFlow
    val protocol: ConnectionProtocol

    fun onDisconnect(reason: String)
}