package dev.slne.surf.cloud.core.common.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener

interface ServerInitializePacketListener: ServerboundPacketListener {

    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.INITIALIZE

    fun handleIdRequest(packet: ServerboundInitializeRequestIdPacket)
}