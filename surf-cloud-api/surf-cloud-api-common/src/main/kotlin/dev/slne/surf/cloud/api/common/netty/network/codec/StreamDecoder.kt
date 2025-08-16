package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

@FunctionalInterface
fun interface StreamDecoder<I, T> {
    fun decode(buf: I): T
}

inline fun <I, T> StreamDecoder<I, T>.decodeFromByteArray(
    creator: (ByteArray) -> I,
    from: ByteArray
): T {
    return decode(creator(from))
}

inline fun <I : ByteBuf, T> StreamDecoder<I, T>.decodeFromByteArray(
    from: ByteArray,
    creator: (ByteBuf) -> I
) = decodeFromByteArray({ creator(Unpooled.wrappedBuffer(it)) }, from)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> StreamDecoder<SurfByteBuf, T>.decodeFromByteArray(from: ByteArray) =
    decodeFromByteArray(from) { SurfByteBuf(it) }
