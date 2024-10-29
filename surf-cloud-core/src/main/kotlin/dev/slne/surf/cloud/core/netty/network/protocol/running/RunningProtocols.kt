package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder

object RunningProtocols {
    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableClientboundProtocol<RunningClientPacketListener, SurfByteBuf>(
            ConnectionProtocol.RUNNING
        ) { builder ->
        }


    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableServerboundProtocol<RunningServerPacketListener, SurfByteBuf>(
            ConnectionProtocol.RUNNING
        ) { builder ->
        }
}