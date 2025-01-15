package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import io.netty.buffer.ByteBuf
import kotlin.math.ceil

/**
 * Utility object for encoding and decoding VarInt values using a space-efficient variable-length representation.
 *
 * VarInt is a common format in network protocols to represent integers compactly, with smaller values
 * consuming fewer bytes. This implementation supports reading and writing VarInts to [ByteBuf].
 */
object VarInt {
    private const val MAX_VARINT_SIZE = 5
    private const val DATA_BITS_MASK = 0x7F
    private const val CONTINUATION_BIT_MASK = 0x80
    private const val DATA_BITS_PER_BYTE = 7

    private val varIntByteLengths =
        IntArray(33) { if (it == 32) 1 else ceil((31.0 - (it - 1)) / DATA_BITS_PER_BYTE).toInt() }

    /**
     * Calculates the number of bytes required to encode the given integer as a VarInt.
     *
     * @param value The integer value to encode.
     * @return The number of bytes needed to represent the value as a VarInt.
     */
    fun getEncodedSize(value: Int): Int = varIntByteLengths[Integer.numberOfLeadingZeros(value)]

    /**
     * Determines if the given byte has the continuation bit set.
     *
     * @param byte The byte to check.
     * @return `true` if the continuation bit is set, otherwise `false`.
     */
    fun hasContinuationBit(byte: Byte): Boolean =
        (byte.toInt() and CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK

    /**
     * Decodes a VarInt from the specified [ByteBuf].
     *
     * @param buf The [ByteBuf] to read from.
     * @return The decoded integer value.
     * @throws RuntimeException If the VarInt exceeds the maximum size.
     */
    fun readVarInt(buf: ByteBuf): Int {
        var result = 0
        var shift = 0

        var currentByte: Byte
        do {
            currentByte = buf.readByte()
            result =
                result or ((currentByte.toInt() and DATA_BITS_MASK) shl (shift++ * DATA_BITS_PER_BYTE))
            checkDecoded(shift <= MAX_VARINT_SIZE) { "VarInt too big" }
        } while (hasContinuationBit(currentByte))

        return result
    }

    /**
     * Encodes the given integer as a VarInt and writes it to the specified [ByteBuf].
     *
     * @param buffer The [ByteBuf] to write to.
     * @param value The integer value to encode.
     * @return The same [ByteBuf] instance for chaining.
     */
    fun writeVarInt(buffer: ByteBuf, value: Int): ByteBuf {
        // Handle one and two byte cases explicitly for improved performance in common cases.
        if ((value and ((0xFFFFFFFF.toInt() shl DATA_BITS_PER_BYTE))) == 0) {
            buffer.writeByte(value)
        } else if ((value and (0xFFFFFFFF.toInt() shl (DATA_BITS_PER_BYTE * 2))) == 0) {
            val combinedValue =
                (value and DATA_BITS_MASK or CONTINUATION_BIT_MASK) shl 8 or (value ushr DATA_BITS_PER_BYTE)
            buffer.writeShort(combinedValue)
        } else {
            writeComplexVarInt(buffer, value)
        }

        return buffer
    }

    /**
     * Encodes a larger integer value as a VarInt and writes it to the [ByteBuf].
     * This method handles cases where more than two bytes are required for encoding.
     *
     * @param buffer The [ByteBuf] to write to.
     * @param value The integer value to encode.
     * @return The same [ByteBuf] instance for chaining.
     */
    private fun writeComplexVarInt(buffer: ByteBuf, value: Int): ByteBuf {
        var value = value
        while ((value and -CONTINUATION_BIT_MASK) != 0) {
            buffer.writeByte(value and DATA_BITS_MASK or CONTINUATION_BIT_MASK)
            value = value ushr DATA_BITS_PER_BYTE
        }

        buffer.writeByte(value)
        return buffer
    }
}