package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx

import dev.slne.surf.bytebufserializer.KBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi

@InternalApi
abstract class CloudBufSerializer<T> : KBufSerializer<T, SurfByteBuf> {
    override val bufClass = SurfByteBuf::class
}