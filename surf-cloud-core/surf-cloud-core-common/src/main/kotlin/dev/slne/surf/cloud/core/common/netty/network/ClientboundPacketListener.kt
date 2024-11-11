package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow

interface ClientboundPacketListener : PacketListener {
    override val flow get() = PacketFlow.CLIENTBOUND
}