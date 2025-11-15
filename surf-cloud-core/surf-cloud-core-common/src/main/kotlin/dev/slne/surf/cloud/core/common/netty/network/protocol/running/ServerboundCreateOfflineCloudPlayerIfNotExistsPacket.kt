package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonServerSynchronizingRunningPacketListener
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:create_offline_cloud_player_if_not_exists",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundCreateOfflineCloudPlayerIfNotExistsPacket(
    val uuid: UUID
) : NettyPacket(), InternalNettyPacket<CommonServerSynchronizingRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCreateOfflineCloudPlayerIfNotExistsPacket::uuid,
            ::ServerboundCreateOfflineCloudPlayerIfNotExistsPacket
        )
    }

    override fun handle(listener: CommonServerSynchronizingRunningPacketListener) {
        listener.handleCreateOfflineCloudPlayerIfNotExists(this)
    }
}