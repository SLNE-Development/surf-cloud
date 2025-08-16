package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:player_cache/request_full_sync", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheRequestFullSyncPacket(val uuid: @Contextual UUID)