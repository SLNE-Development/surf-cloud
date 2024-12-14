package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder

object PreRunningProtocols {
    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerPreLoginPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)

    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientPreRunningPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf)
}