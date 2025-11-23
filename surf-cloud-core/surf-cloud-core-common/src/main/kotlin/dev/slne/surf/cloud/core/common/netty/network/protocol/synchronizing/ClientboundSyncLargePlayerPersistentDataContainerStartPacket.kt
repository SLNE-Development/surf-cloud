package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:sync_large_player_ppdc/start",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSyncLargePlayerPersistentDataContainerStartPacket(val playerUuid: UUID) :
    NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSyncLargePlayerPersistentDataContainerStartPacket::playerUuid,
            ::ClientboundSyncLargePlayerPersistentDataContainerStartPacket
        )
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSyncLargerPlayerPersistentDataContainerStart(this)
    }
}