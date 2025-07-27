package dev.slne.surf.cloud.core.common.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.serverbound.ServerboundInitializeRequestIdPacket

interface ServerInitializePacketListener : ServerboundPacketListener {

    override val protocol: ConnectionProtocol
        get() = ConnectionProtocol.INITIALIZE

    fun handleIdRequest(packet: ServerboundInitializeRequestIdPacket)
}