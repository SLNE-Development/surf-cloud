package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.CacheKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:player_cache/remove_key", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheRemoveKeyPacket(
    val playerId: @Contextual UUID,
    val key: CacheKey<*>,
    val changeId: Long
) : NettyPacket()