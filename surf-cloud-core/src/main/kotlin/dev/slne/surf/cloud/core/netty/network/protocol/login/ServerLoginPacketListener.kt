package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.netty.network.TickablePacketListener

interface ServerLoginPacketListener: ServerboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.LOGIN

    fun handleLoginStart(packet: ServerboundLoginStartPacket)

    suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket)
}