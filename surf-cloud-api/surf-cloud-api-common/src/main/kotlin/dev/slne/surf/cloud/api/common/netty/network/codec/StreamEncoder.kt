package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

@FunctionalInterface
fun interface StreamEncoder<O, T> {
    fun encode(buf: O, value: T)
}


@JvmName("encodeToByteArrayWithByteArray")
inline fun <O, T> StreamEncoder<O, T>.encodeToByteArray(
    size: Int,
    creator: (ByteArray) -> O,
    value: T
): ByteArray {
    val arr = ByteArray(size)
    val out = creator(arr)
    encode(out, value)
    return arr
}

inline fun <O : ByteBuf, T> StreamEncoder<O, T>.encodeToByteArray(
    size: Int,
    creator: (ByteBuf) -> O,
    value: T
): ByteArray {
    val arr = ByteArray(size)
    val buf = Unpooled.wrappedBuffer(arr)
    buf.clear()
    val out = creator(buf)
    encode(out, value)

    val written = buf.writerIndex()
    return if (written == arr.size) arr else arr.copyOf(written)
}

inline fun <O : ByteBuf, T> StreamEncoder<O, T>.encodeInto(
    target: ByteArray,
    creator: (ByteBuf) -> O,
    value: T
): Int {
    val buf = Unpooled.wrappedBuffer(target)
    buf.clear()
    val out = creator(buf)
    encode(out, value)
    return buf.writerIndex()
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> StreamEncoder<SurfByteBuf, T>.encodeToByteArray(
    size: Int,
    value: T
): ByteArray = encodeToByteArray(size, { buf: ByteBuf -> SurfByteBuf(buf) }, value)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> StreamEncoder<SurfByteBuf, T>.encodeInto(
    target: ByteArray,
    value: T
): Int = encodeInto(target, { SurfByteBuf(it) }, value)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> StreamEncoder<SurfByteBuf, T>.encodeToByteArray(
    value: T,
    sizeOf: (T) -> Int
): ByteArray = encodeToByteArray(sizeOf(value), value)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> StreamEncoder<SurfByteBuf, T>.encodeToByteArrayDynamic(
    value: T,
    initialCapacity: Int = 256
): ByteArray {
    val buf = Unpooled.buffer(initialCapacity) // Heap-Buffer
    encode(SurfByteBuf(buf), value)
    val written = buf.writerIndex()
    val out = ByteArray(written)
    buf.getBytes(0, out)
    return out
}