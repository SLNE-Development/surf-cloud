package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.createCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundBatchUpdateServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundCreateOfflineCloudPlayerIfNotExistsPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket

object SynchronizingProtocols {

    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableClientboundProtocol<ClientSynchronizingPacketListener, SurfByteBuf>(
            ConnectionProtocol.SYNCHRONIZING
        ) { builder ->
            builder.withBundlePacket(::ClientboundBundlePacket, ClientboundBundleDelimiterPacket())
            builder.addPacket(KeepAlivePacket.STREAM_CODEC)
                .addPacket(ClientboundPongResponsePacket.STREAM_CODEC)
                .addPacket(ClientboundBatchUpdateServer.STREAM_CODEC)
                .addPacket(SyncValueChangePacket.STREAM_CODEC)
                .addPacket(ClientboundBatchSyncValuePacket.STREAM_CODEC)
                .addPacket(ClientboundBatchSyncSetPacket.STREAM_CODEC)
                .addPacket(SyncSetDeltaPacket.STREAM_CODEC)
                .addPacket(FinishSynchronizingPacket.STREAM_CODEC)
                .addPacket(ClientboundSynchronizeFinishPacket.STREAM_CODEC)
                .addPacket(ClientboundSetVelocitySecretPacket.STREAM_CODEC)
        }

    val CLIENTBOUND by lazy { CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf) }

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableServerboundProtocol<ServerSynchronizingPacketListener, SurfByteBuf>(
            ConnectionProtocol.SYNCHRONIZING
        ) { builder ->
            builder.withBundlePacket(::ServerboundBundlePacket, ServerboundBundleDelimiterPacket())
            builder.addPacket(KeepAlivePacket.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(ServerboundSynchronizeFinishAcknowledgedPacket.STREAM_CODEC)
                .addPacket(SyncValueChangePacket.STREAM_CODEC)
                .addPacket(SyncSetDeltaPacket.STREAM_CODEC)
                .addPacket(FinishSynchronizingPacket.STREAM_CODEC)
                .addPacket(ServerboundCreateOfflineCloudPlayerIfNotExistsPacket.STREAM_CODEC)
                .addPacket(ServerboundCacheRegisterKeysPacket::class.createCodec())
        }

    val SERVERBOUND by lazy { SERVERBOUND_TEMPLATE.bind(::SurfByteBuf) }
}