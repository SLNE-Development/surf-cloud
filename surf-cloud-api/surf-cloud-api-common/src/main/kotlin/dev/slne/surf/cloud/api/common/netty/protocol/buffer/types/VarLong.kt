package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import io.netty.buffer.ByteBuf

/**
 * Utility object for encoding and decoding VarLong values using a variable-length representation.
 *
 * VarLong is a compact format for encoding long integers, where smaller values use fewer bytes.
 * It is particularly useful in network protocols to optimize bandwidth usage.
 */
object VarLong {
    private const val MAX_VARLONG_SIZE = 10
    private const val DATA_BITS_MASK = 0x7F
    private const val CONTINUATION_BIT_MASK = 0x80
    private const val DATA_BITS_PER_BYTE = 7

    /**
     * Calculates the number of bytes required to encode the given long value as a VarLong.
     *
     * @param value The long value to encode.
     * @return The number of bytes needed to represent the value as a VarLong.
     */
    fun getEncodedSize(value: Long): Int {
        for (i in 1 until MAX_VARLONG_SIZE) {
            if ((value and -1L shl i * DATA_BITS_PER_BYTE) == 0L) {
                return i
            }
        }

        return MAX_VARLONG_SIZE
    }

    /**
     * Determines if the given byte has the continuation bit set.
     *
     * @param byte The byte to check.
     * @return `true` if the continuation bit is set, otherwise `false`.
     */
    fun hasContinuationBit(byte: Byte): Boolean =
        (byte.toInt() and CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK

    /**
     * Decodes a VarLong from the specified [ByteBuf].
     *
     * @param buf The [ByteBuf] to read from.
     * @return The decoded long value.
     * @throws RuntimeException If the VarLong exceeds the maximum size.
     */
    fun readVarLong(buf: ByteBuf): Long {
        var result = 0L
        var shift = 0

        var currentByte: Byte
        do {
            currentByte = buf.readByte()
            result =
                result or ((currentByte.toLong() and DATA_BITS_MASK.toLong()) shl (shift++ * DATA_BITS_PER_BYTE))
            shift += DATA_BITS_PER_BYTE

            checkDecoded(shift <= MAX_VARLONG_SIZE) { "VarLong too big" }
        } while (currentByte.toInt() and CONTINUATION_BIT_MASK == CONTINUATION_BIT_MASK)

        return result
    }

    /**
     * Encodes the given long value as a VarLong and writes it to the specified [ByteBuf].
     *
     * @param buf The [ByteBuf] to write to.
     * @param value The long value to encode.
     * @return The same [ByteBuf] instance for chaining.
     */
    fun writeVarLong(buf: ByteBuf, value: Long): ByteBuf {
        var value = value

        while ((value and -CONTINUATION_BIT_MASK.toLong()) != 0L) {
            buf.writeByte((value and DATA_BITS_MASK.toLong()).toInt() or CONTINUATION_BIT_MASK)
            value = value ushr DATA_BITS_PER_BYTE
        }
        buf.writeByte(value.toInt())

        return buf
    }
}