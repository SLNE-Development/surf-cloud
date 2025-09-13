package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import kotlinx.serialization.Contextual
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:player_cache/delta",
    PacketFlow.CLIENTBOUND
)
class ClientboundCacheDeltaPacket(
    val uuid: @Contextual UUID,
    val key: CacheNetworkKey,
    val newVersion: Long,
    val kind: ServerboundCacheOpPacket.Kind,
    val payload: ByteArray
) : NettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundCacheDeltaPacket::uuid,
            CacheNetworkKey.STREAM_CODEC,
            ClientboundCacheDeltaPacket::key,
            ByteBufCodecs.LONG_CODEC,
            ClientboundCacheDeltaPacket::newVersion,
            ByteBufCodecs.enumStreamCodec<ServerboundCacheOpPacket.Kind>(),
            ClientboundCacheDeltaPacket::kind,
            ByteBufCodecs.BYTE_ARRAY_CODEC,
            ClientboundCacheDeltaPacket::payload,
            ::ClientboundCacheDeltaPacket
        )
    }
}