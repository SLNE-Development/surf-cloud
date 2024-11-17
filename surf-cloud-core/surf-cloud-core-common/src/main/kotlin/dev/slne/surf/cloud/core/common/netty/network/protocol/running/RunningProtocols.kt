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
                .addPacket(ClientboundSendMessagePacket.STREAM_CODEC)
                .addPacket(ClientboundSendActionBarPacket.STREAM_CODEC)
                .addPacket(ClientboundSendPlayerListHeaderAndFooterPacket.STREAM_CODEC)
                .addPacket(ClientboundShowTitlePacket.STREAM_CODEC)
                .addPacket(ClientboundSendTitlePartPacket.STREAM_CODEC)
                .addPacket(ClientboundClearTitlePacket.STREAM_CODEC)
                .addPacket(ClientboundResetTitlePacket.STREAM_CODEC)
                .addPacket(ClientboundShowBossBarPacket.STREAM_CODEC)
                .addPacket(ClientboundHideBossBarPacket.STREAM_CODEC)
                .addPacket(ClientboundPlaySoundPacket.STREAM_CODEC)
                .addPacket(ClientboundStopSoundPacket.STREAM_CODEC)
                .addPacket(ClientboundOpenBookPacket.STREAM_CODEC)
                .addPacket(ClientboundSendResourcePacksPacket.STREAM_CODEC)
                .addPacket(ClientboundRemoveResourcePacksPacket.STREAM_CODEC)
                .addPacket(ClientboundClearResourcePacks.STREAM_CODEC)
                .addPacket(PlayerConnectToServerPacket.STREAM_CODEC)
                .addPacket(PlayerDisconnectFromServerPacket.STREAM_CODEC)
        }

    val CLIENTBOUND by lazy { CLIENTBOUND_TEMPLATE.freeze().bind(::SurfByteBuf) }

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.mutableServerboundProtocol<RunningServerPacketListener, SurfByteBuf>(
            ConnectionProtocol.RUNNING
        ) { builder ->
            builder.addPacket(ServerboundKeepAlivePacket.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(ServerboundPongPacket.STREAM_CODEC)
                .addPacket(ServerboundSendMessagePacket.STREAM_CODEC)
                .addPacket(ServerboundSendActionBarPacket.STREAM_CODEC)
                .addPacket(ServerboundSendPlayerListHeaderAndFooter.STREAM_CODEC)
                .addPacket(ServerboundShowTitlePacket.STREAM_CODEC)
                .addPacket(ServerboundSendTitlePartPacket.STREAM_CODEC)
                .addPacket(ServerboundClearTitlePacket.STREAM_CODEC)
                .addPacket(ServerboundResetTitlePacket.STREAM_CODEC)
                .addPacket(ServerboundShowBossBarPacket.STREAM_CODEC)
                .addPacket(ServerboundHideBossBarPacket.STREAM_CODEC)
                .addPacket(ServerboundPlaySoundPacket.STREAM_CODEC)
                .addPacket(ServerboundStopSoundPacket.STREAM_CODEC)
                .addPacket(ServerboundOpenBookPacket.STREAM_CODEC)
                .addPacket(ServerboundSendResourcePacksPacket.STREAM_CODEC)
                .addPacket(ServerboundRemoveResourcePacksPacket.STREAM_CODEC)
                .addPacket(ServerboundClearResourcePacks.STREAM_CODEC)
                .addPacket(ServerboundBroadcastPacket.STREAM_CODEC)
                .addPacket(ServerboundClientInformationPacket.STREAM_CODEC)
                .addPacket(ServerboundPingRequestPacket.STREAM_CODEC)
                .addPacket(PlayerConnectToServerPacket.STREAM_CODEC)
                .addPacket(PlayerDisconnectFromServerPacket.STREAM_CODEC)
        }

    val SERVERBOUND by lazy { SERVERBOUND_TEMPLATE.freeze().bind(::SurfByteBuf) }
}