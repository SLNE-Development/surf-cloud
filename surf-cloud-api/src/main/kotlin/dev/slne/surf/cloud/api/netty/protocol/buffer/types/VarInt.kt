package dev.slne.surf.cloud.api.netty.protocol.buffer.types

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

//    private val varIntByteLengths = IntArray(33) {
//        if (it == 32) 1 else ceil((32.0 - (it - 1)) / DATA_BITS_PER_BYTE).toInt()
//    }

    private val varIntByteLengths: IntArray = IntArray(33)

    init {
        for (i in 0..32) {
            varIntByteLengths[i] = ceil((31.0 - (i - 1)) / DATA_BITS_PER_BYTE.toDouble()).toInt()
        }

        varIntByteLengths[32] = 1
    }

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
                result or ((currentByte.toInt() and DATA_BITS_MASK) shl shift++ * DATA_BITS_PER_BYTE)

            if (shift > MAX_VARINT_SIZE) {
                throw RuntimeException("VarInt too big")
            }

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
        if ((value and (-0x1 shl DATA_BITS_PER_BYTE)) == 0) {
            buffer.writeByte(value)
        } else if ((value and (-0x1 shl 14)) == 0) {
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
        var mutableValue = value
        while ((mutableValue and CONTINUATION_BIT_MASK.inv()) != 0) {
            buffer.writeByte(mutableValue and DATA_BITS_MASK or CONTINUATION_BIT_MASK)
            mutableValue = mutableValue ushr DATA_BITS_PER_BYTE
        }

        buffer.writeByte(mutableValue)
        return buffer
    }
}