package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*

object PreRunningProtocols {
    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientPreRunningPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
            builder.withBundlePacket(::ClientboundBundlePacket, ClientboundBundleDelimiterPacket())
            // TODO: disconnect packet
            builder.addPacket(ClientboundKeepAlivePacket.STREAM_CODEC)
                .addPacket(ClientboundPongResponsePacket.STREAM_CODEC)
                .addPacket(ClientboundPreRunningFinishedPacket.STREAM_CODEC)
                .addPacket(ClientboundReadyToRunPacket.STREAM_CODEC)
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf)

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerPreRunningPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
            builder.withBundlePacket(::ServerboundBundlePacket, ServerboundBundleDelimiterPacket())
            builder.addPacket(ServerboundKeepAlivePacket.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(ServerboundPreRunningAcknowledgedPacket.STREAM_CODEC)
                .addPacket(ServerboundReadyToRunPacket.STREAM_CODEC)
                .addPacket(ServerboundRequestContinuation.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)
}