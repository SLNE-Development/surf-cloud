package dev.slne.surf.cloud.core.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.handshake.serverbound.ServerboundHandshakePacket

object HandshakeProtocols {
    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerHandshakePacketListener, SurfByteBuf>(
            ConnectionProtocol.HANDSHAKING
        ) { builder ->
            builder.addPacket(ServerboundHandshakePacket.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)
}