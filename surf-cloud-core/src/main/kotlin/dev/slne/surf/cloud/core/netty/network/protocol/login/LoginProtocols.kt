package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder

object LoginProtocols {

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerLoginPacketListener, SurfByteBuf>(
            ConnectionProtocol.LOGIN
        ) { builder ->
            builder.addPacket(ServerboundLoginAcknowledgedPacket.STREAM_CODEC)
            builder.addPacket(ServerboundLoginStartPacket.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)

    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientLoginPacketListener, SurfByteBuf>(
            ConnectionProtocol.LOGIN
        ) { builder ->
            builder.addPacket(ClientboundLoginDisconnectPacket.STREAM_CODEC)
            builder.addPacket(ClientboundLoginFinishedPacket.STREAM_CODEC)
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf)
}