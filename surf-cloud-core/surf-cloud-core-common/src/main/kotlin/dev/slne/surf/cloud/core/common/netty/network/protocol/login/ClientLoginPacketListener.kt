package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener

interface ClientLoginPacketListener: ClientboundPacketListener {
    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.LOGIN

    suspend fun handleLoginFinished(packet: ClientboundLoginFinishedPacket)
}