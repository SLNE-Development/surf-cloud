package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:player_cache/error",
    PacketFlow.CLIENTBOUND
)
@Serializable
class ClientboundCacheErrorPacket(
    val uuid: @Contextual UUID,
    val key: CacheNetworkKey,
    val errorCode: ErrorCode,
): NettyPacket() {
    enum class ErrorCode {
        VERSION_CONFLICT,
        UNKNOWN_KEY
    }
}