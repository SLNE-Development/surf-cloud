package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener

interface ServerLoginPacketListener: ServerboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.LOGIN

    fun handleLoginStart(packet: ServerboundLoginStartPacket)

    fun handleWaitForServerToStart(packet: ServerboundWaitForServerToStartPacket)

    suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket)
}