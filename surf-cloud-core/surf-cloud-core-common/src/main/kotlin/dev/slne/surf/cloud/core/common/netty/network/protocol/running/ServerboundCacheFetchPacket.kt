package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:player_cache/fetch",
    PacketFlow.SERVERBOUND
)
@Serializable
class ServerboundCacheFetchPacket(val uuid: @Contextual UUID) :
    RespondingNettyPacket<ClientboundCacheSnapshotPacket>()