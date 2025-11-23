package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:clientbound:sync_large_player_ppdc/chunk",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSyncLargePlayerPersistentDataContainerChunkPacket(val payload: ByteArray) :
    NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY_CODEC,
            ClientboundSyncLargePlayerPersistentDataContainerChunkPacket::payload,
            ::ClientboundSyncLargePlayerPersistentDataContainerChunkPacket
        )
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSyncLargerPlayerPersistentDataContainerChunk(this)
    }
}