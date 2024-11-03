package dev.slne.surf.cloud.core.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ClientboundPacketListener

interface ClientInitializePacketListener: ClientboundPacketListener {
    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.INITIALIZE

    fun handleIdResponse(packet: ClientboundInitializeIdResponsePacket)
}