package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.surfapi.core.api.util.logger

interface ClientboundPacketListener : PacketListener {
    override val flow get() = PacketFlow.CLIENTBOUND

    override fun onPacketError(
        packet: NettyPacket,
        exception: Throwable
    ) {
        log.atSevere()
            .withCause(exception)
            .log("Failed to handle packet '%s'", packet)
    }

    companion object {
        private val log = logger()
    }
}