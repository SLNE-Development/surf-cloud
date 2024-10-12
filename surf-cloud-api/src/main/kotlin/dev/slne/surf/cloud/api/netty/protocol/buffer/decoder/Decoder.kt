package dev.slne.surf.cloud.api.netty.protocol.buffer.decoder

import io.netty.buffer.ByteBuf

/**
 * Functional interface representing a decoder that decodes a value of type [T] from a buffer of type [B].
 *
 * @param B The type of the buffer (e.g., [ByteBuf]).
 * @param T The type of the value to decode.
 */
@FunctionalInterface
fun interface Decoder<B : ByteBuf, T> {

    /**
     * Decodes a value of type [T] from the given buffer.
     *
     * @param buffer The buffer from which to decode the value.
     * @return The decoded value of type [T].
     */
    fun decode(buffer: B): T
}
