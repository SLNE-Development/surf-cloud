package dev.slne.surf.cloud.core.common.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.clientbound.ClientboundInitializeIdResponsePacket

interface ClientInitializePacketListener : ClientboundPacketListener {
    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.INITIALIZE

    fun handleIdResponse(packet: ClientboundInitializeIdResponsePacket)
}