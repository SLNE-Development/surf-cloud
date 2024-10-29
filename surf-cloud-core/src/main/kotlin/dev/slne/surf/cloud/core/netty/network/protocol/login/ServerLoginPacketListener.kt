package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginStartPacket

interface ServerLoginPacketListener: ServerboundPacketListener {
    override val protocol get() = ConnectionProtocol.LOGIN

    fun handleLoginStart(packet: ServerboundLoginStartPacket)

    suspend fun handleLoginAcknowledgement(packet: ServerboundLoginAcknowledgedPacket)
}