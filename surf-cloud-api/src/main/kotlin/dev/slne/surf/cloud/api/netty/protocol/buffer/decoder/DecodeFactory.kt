package dev.slne.surf.cloud.api.netty.protocol.buffer.decoder

import io.netty.buffer.ByteBuf

/**
 * Functional interface representing a factory for decoding values of type [T] from a buffer of type [B].
 *
 * @param B The type of the buffer (e.g., [ByteBuf]).
 * @param T The type of the value to decode.
 */
fun interface DecodeFactory<B : ByteBuf, T> {
    /**
     * Decodes a value of type [T] from the given buffer.
     *
     * @param buffer The buffer from which to decode the value.
     * @return The decoded value of type [T].
     */
    operator fun invoke(buffer: B): T

    /**
     * Functional interface representing a factory for decoding [Long] values from a buffer of type [B].
     *
     * @param B The type of the buffer (e.g., [ByteBuf]).
     */
    @FunctionalInterface
    fun interface DecodeLongFactory<B : ByteBuf> : DecodeFactory<B, Long> {

        /**
         * Decodes a [Long] value from the given buffer.
         *
         * @param buffer The buffer from which to decode the value.
         * @return The decoded [Long] value.
         */
        @Deprecated("Use decodeLong instead", ReplaceWith("decodeLong(buffer)"))
        override operator fun invoke(buffer: B) = decodeLong(buffer)

        /**
         * Decodes a [Long] value from the given buffer.
         *
         * @param buffer The buffer from which to decode the value.
         * @return The decoded [Long] value.
         */
        fun decodeLong(buffer: B): Long
    }
}