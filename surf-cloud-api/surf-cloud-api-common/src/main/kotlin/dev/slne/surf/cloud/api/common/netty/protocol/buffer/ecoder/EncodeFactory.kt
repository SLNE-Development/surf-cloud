package dev.slne.surf.cloud.api.common.netty.protocol.buffer.ecoder

import io.netty.buffer.ByteBuf

/**
 * The interface Encode factory.
 *
 * @param <B> the type parameter
 * @param <T> the type parameter
</T></B> */
fun interface EncodeFactory<B : ByteBuf, T> {
    /**
     * Encode.
     *
     * @param buffer the buffer
     * @param value  the value
     */
    operator fun invoke(buffer: B, value: T)

    @FunctionalInterface
    fun interface EncodeLongFactory<B : ByteBuf> : EncodeFactory<B, Long> {
        @Deprecated("Use encodeLong instead", ReplaceWith("encodeLong(buffer, value)"))
        override operator fun invoke(buffer: B, value: Long) = encodeLong(buffer, value)
        fun encodeLong(buffer: B, value: Long)
    }
}

