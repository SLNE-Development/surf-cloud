package dev.slne.surf.cloud.api.common.player.cache

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

interface StructuredType<T : Any, D : Any> {
    val valueCodec: StreamCodec<SurfByteBuf, T>
    val deltaCodec: StreamCodec<SurfByteBuf, D>

    fun applyDelta(current: T, delta: D): T
}