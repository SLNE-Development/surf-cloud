package dev.slne.surf.cloud.api.netty.protocol.buffer.codec

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException

/**
 * Interface representing a codec capable of encoding and decoding values of type [T].
 * It provides methods to convert objects to and from binary representations using a [SurfByteBuf].
 *
 * @param T The type of the object being encoded/decoded.
 */
interface Codec<T> {

    /**
     * Decodes a value of type [T] from the given [SurfByteBuf].
     *
     * @param buf The [SurfByteBuf] from which to decode the value.
     * @return The decoded value of type [T].
     * @throws DecoderException If an error occurs during decoding.
     */
    @Throws(DecoderException::class)
    fun decode(buf: SurfByteBuf): T

    /**
     * Encodes a value of type [T] to the given [SurfByteBuf].
     *
     * @param buf The [SurfByteBuf] to which the value will be encoded.
     * @param value The value of type [T] to encode.
     * @throws EncoderException If an error occurs during encoding.
     */
    @Throws(EncoderException::class)
    fun encode(buf: SurfByteBuf, value: T)

    /**
     * Functional interface for decoding values from a [SurfByteBuf].
     *
     * @param D The type of the object being decoded.
     */
    fun interface Decoder<D> {

        /**
         * Decodes a value of type [D] from the given [SurfByteBuf].
         *
         * @param buf The [SurfByteBuf] from which to decode the value.
         * @return The decoded value of type [D].
         * @throws DecoderException If an error occurs during decoding.
         */
        @Throws(DecoderException::class)
        fun decode(buf: SurfByteBuf): D
    }

    /**
     * Functional interface for encoding values to a [SurfByteBuf].
     *
     * @param T The type of the object being encoded.
     */
    fun interface Encoder<T> {

        /**
         * Encodes a value of type [T] to the given [SurfByteBuf].
         *
         * @param buf The [SurfByteBuf] to which the value will be encoded.
         * @param value The value of type [T] to encode.
         * @throws EncoderException If an error occurs during encoding.
         */
        @Throws(EncoderException::class)
        fun encode(buf: SurfByteBuf, value: T)
    }

    companion object {
        /**
         * Creates a new [Codec] from the given encoder and decoder.
         *
         * @param T The type of the object being encoded/decoded.
         * @param encoder The [Encoder] to use for encoding values of type [T].
         * @param decoder The [Decoder] to use for decoding values of type [T].
         * @return A [Codec] instance that uses the provided encoder and decoder.
         */
        fun <T> codec(
            encoder: Encoder<T>,
            decoder: Decoder<T>
        ) = object : Codec<T> {
            override fun decode(buf: SurfByteBuf) = decoder.decode(buf)
            override fun encode(buf: SurfByteBuf, value: T) = encoder.encode(buf, value)
        }
    }
}

