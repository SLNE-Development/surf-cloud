package dev.slne.surf.cloud.core.common.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener

interface ServerHandshakePacketListener: ServerboundPacketListener {

    override val protocol get() = ConnectionProtocol.HANDSHAKING

    suspend fun handleHandshake(packet: ServerboundHandshakePacket)
}