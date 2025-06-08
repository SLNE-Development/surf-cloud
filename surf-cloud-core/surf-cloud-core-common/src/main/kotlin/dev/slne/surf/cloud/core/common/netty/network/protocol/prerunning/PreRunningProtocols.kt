package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.long.LongResponse
import dev.slne.surf.cloud.api.common.netty.packet.createCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*

object PreRunningProtocols {
    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientPreRunningPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
            builder.withBundlePacket(::ClientboundBundlePacket, ClientboundBundleDelimiterPacket())
            // TODO: disconnect packet - needed?
            builder.addPacket(KeepAlivePacket::class.createCodec())
                .addPacket(LongResponse.STREAM_CODEC)
                .addPacket(ClientboundPongResponsePacket.STREAM_CODEC)
                .addPacket(ClientboundPreRunningFinishedPacket.STREAM_CODEC)
                .addPacket(ClientboundProceedToSynchronizingPacket.STREAM_CODEC)
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf)

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerPreRunningPacketListener, SurfByteBuf>(
            ConnectionProtocol.PRE_RUNNING
        ) { builder ->
            builder.withBundlePacket(::ServerboundBundlePacket, ServerboundBundleDelimiterPacket())
            builder.addPacket(KeepAlivePacket::class.createCodec())
                .addPacket(LongResponse.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(ServerboundPreRunningAcknowledgedPacket.STREAM_CODEC)
                .addPacket(ServerboundProceedToSynchronizingAcknowledgedPacket.STREAM_CODEC)
                .addPacket(ServerboundRequestContinuation.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)
}