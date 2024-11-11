package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder

object RunningProtocols {
    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableClientboundProtocol<RunningClientPacketListener, SurfByteBuf>(
            ConnectionProtocol.RUNNING
        ) { builder ->
            builder.withBundlePacket(::ClientboundBundlePacket, ClientboundBundleDelimiterPacket())
            builder.addPacket(ClientboundDisconnectPacket.STREAM_CODEC)
                .addPacket(ClientboundKeepAlivePacket.STREAM_CODEC)
                .addPacket(ClientboundPingPacket.STREAM_CODEC)
                .addPacket(ClientboundPongResponsePacket.STREAM_CODEC)
        }

    val CLIENTBOUND by lazy { CLIENTBOUND_TEMPLATE.freeze().bind(::SurfByteBuf) }

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableServerboundProtocol<RunningServerPacketListener, SurfByteBuf>(
            ConnectionProtocol.RUNNING
        ) { builder ->
            builder.addPacket(ServerboundKeepAlivePacket.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(ServerboundPongPacket.STREAM_CODEC)
        }

    val SERVERBOUND by lazy { SERVERBOUND_TEMPLATE.freeze().bind(::SurfByteBuf) }
}