package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:player_cache/hydrate_end",
    PacketFlow.CLIENTBOUND
)
@Serializable
class ClientboundPlayerCacheHydrateEndPacket(val uuid: @Contextual UUID) : NettyPacket()