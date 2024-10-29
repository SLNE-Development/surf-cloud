package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ClientboundPacketListener

interface ClientLoginPacketListener: ClientboundPacketListener {
    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.LOGIN
}