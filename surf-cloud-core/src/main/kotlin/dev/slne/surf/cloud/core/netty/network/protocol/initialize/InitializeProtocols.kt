package dev.slne.surf.cloud.core.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder
import io.netty.buffer.ByteBuf

object InitializeProtocols {
    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerInitializePacketListener, ByteBuf>(
            ConnectionProtocol.INITIALIZE
        ) { builder ->
            builder.addPacket(ServerboundInitializeRequestIdPacket.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind { buf -> buf }

    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientInitializePacketListener, ByteBuf>(
            ConnectionProtocol.INITIALIZE
        ) { builder ->
            builder.addPacket(ClientboundInitializeIdResponsePacket.STREAM_CODEC)
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind { buf -> buf }
}