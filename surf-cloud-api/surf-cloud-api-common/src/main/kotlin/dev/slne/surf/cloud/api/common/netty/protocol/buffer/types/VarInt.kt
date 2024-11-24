package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import io.netty.buffer.ByteBuf
import kotlin.math.ceil

/**
 * Utility object for encoding and decoding VarInt values from a ByteBuf.
 * VarInt is a variable-length integer representation commonly used in network protocols to save space.
 */
object VarInt {
    private const val MAX_VARINT_SIZE = 5
    private const val DATA_BITS_MASK = 0x7F
    private const val CONTINUATION_BIT_MASK = 0x80
    private const val DATA_BITS_PER_BYTE = 7

    private val varIntByteLengths =
        IntArray(33) { if (it == 32) 1 else ceil((31.0 - (it - 1)) / DATA_BITS_PER_BYTE).toInt() }

    /**
     * Gets the exact byte size needed to encode the given value as a VarInt.
     *
     * @param value The integer value to encode.
     * @return The number of bytes required to encode the given value as a VarInt.
     */
    fun getEncodedSize(value: Int): Int = varIntByteLengths[Integer.numberOfLeadingZeros(value)]

    /**
     * Checks if the given byte has the continuation bit set.
     *
     * @param byte The byte to check.
     * @return True if the continuation bit is set, otherwise false.
     */
    fun hasContinuationBit(byte: Byte): Boolean =
        (byte.toInt() and CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK

    /**
     * Reads a VarInt value from the given [ByteBuf].
     *
     * @param buf The [ByteBuf] to read the value from.
     * @return The decoded VarInt value.
     * @throws RuntimeException if the VarInt is too large to fit in an integer.
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
     * Writes the given value as a VarInt to the specified [ByteBuf].
     *
     * @param buffer The [ByteBuf] to which the value will be written.
     * @param value The integer value to encode.
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
     * Writes the given value as a VarInt to the specified [ByteBuf] using the standard method.
     * This method is used for values that cannot be encoded in one or two bytes.
     *
     * @param buffer The [ByteBuf] to write the value to.
     * @param value The integer value to encode.
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