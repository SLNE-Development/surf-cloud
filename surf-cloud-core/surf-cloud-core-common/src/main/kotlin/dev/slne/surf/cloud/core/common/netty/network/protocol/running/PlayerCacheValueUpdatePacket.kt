package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:player_cache/value_update", PacketFlow.BIDIRECTIONAL)
@Serializable
data class PlayerCacheValueUpdatePacket(
    val playerId: @Contextual UUID,
    val key: CacheKey<*>,
    val payload: ByteArray,
    val changeId: Long
): NettyPacket() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerCacheValueUpdatePacket) return false

        if (changeId != other.changeId) return false
        if (playerId != other.playerId) return false
        if (key != other.key) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = changeId.hashCode()
        result = 31 * result + playerId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}