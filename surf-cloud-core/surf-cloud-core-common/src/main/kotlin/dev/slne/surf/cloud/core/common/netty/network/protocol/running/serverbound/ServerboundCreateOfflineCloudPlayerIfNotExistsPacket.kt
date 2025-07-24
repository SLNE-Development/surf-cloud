package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:create_offline_cloud_player_if_not_exists",
    PacketFlow.SERVERBOUND
)
@Serializable
class ServerboundCreateOfflineCloudPlayerIfNotExistsPacket(val uuid: @Contextual UUID) :
    NettyPacket()