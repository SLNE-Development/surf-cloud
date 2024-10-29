package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow

interface ServerboundPacketListener: PacketListener {
    override val flow get() = PacketFlow.SERVERBOUND
}