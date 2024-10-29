package dev.slne.surf.cloud.core.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.handshake.serverbound.ServerboundHandshakePacket

interface ServerHandshakePacketListener: ServerboundPacketListener {

    override val protocol get() = ConnectionProtocol.HANDSHAKING

    suspend fun handleHandshake(packet: ServerboundHandshakePacket)
}