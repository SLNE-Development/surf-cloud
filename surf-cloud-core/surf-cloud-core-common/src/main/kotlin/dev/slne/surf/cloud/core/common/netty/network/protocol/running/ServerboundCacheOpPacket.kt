package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:player_cache/cache_op",
    PacketFlow.SERVERBOUND
)
class ServerboundCacheOpPacket(
    val uuid: UUID,
    val key: CacheNetworkKey,
    val expectedVersion: Long,
    val kind: Kind,
    val payload: ByteArray,
) : NettyPacket() { // https://chatgpt.com/g/g-p-68b2beb92d908191a49e4f7256c08277-cloud/c/68b2c056-f214-8332-9e15-c608a8239cf9?model=gpt-5-pro#:~:text=%40SurfNettyPacket(%0A%20%20%20%20DefaultIds.CLIENTBOUND_PLAYER_CACHE_DELTA%2C%0A%20%20%20%20PacketFlow.CLIENTBOUND%0A)%0Aclass%20ClientboundCacheDelta%20%3A%20NettyPacket%20%7B

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCacheOpPacket::uuid,
            CacheNetworkKey.STREAM_CODEC,
            ServerboundCacheOpPacket::key,
            ByteBufCodecs.LONG_CODEC,
            ServerboundCacheOpPacket::expectedVersion,
            ByteBufCodecs.enumStreamCodec<Kind>(),
            ServerboundCacheOpPacket::kind,
            ByteBufCodecs.BYTE_ARRAY_CODEC,
            ServerboundCacheOpPacket::payload,
            ::ServerboundCacheOpPacket
        )
    }


    enum class Kind {
        VALUE_SET,
        LIST_APPEND, LIST_INSERT, LIST_SET, LIST_REMOVE_AT, LIST_CLEAR,
        SET_ADD, SET_REMOVE, SET_CLEAR,
        MAP_PUT, MAP_REMOVE, MAP_CLEAR,
        STRUCTURED_DELTA
    }
}