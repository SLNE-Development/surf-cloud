package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import io.netty.buffer.ByteBuf

object VarLong {
    private const val MAX_VARLONG_SIZE = 10
    private const val DATA_BITS_MASK = 0x7F
    private const val CONTINUATION_BIT_MASK = 0x80
    private const val DATA_BITS_PER_BYTE = 7

    fun getEncodedSize(value: Long): Int {
        for (i in 1 until MAX_VARLONG_SIZE) {
            if ((value and -1L shl i * DATA_BITS_PER_BYTE) == 0L) {
                return i
            }
        }

        return MAX_VARLONG_SIZE
    }

    fun hasContinuationBit(byte: Byte): Boolean =
        (byte.toInt() and CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK

    fun readVarLong(buf: ByteBuf): Long {
        var result = 0L
        var shift = 0

        var currentByte: Byte
        do {
            currentByte = buf.readByte()
            result = result or ((currentByte.toLong() and DATA_BITS_MASK.toLong()) shl (shift++ * DATA_BITS_PER_BYTE))
            shift += DATA_BITS_PER_BYTE

            checkDecoded(shift <= MAX_VARLONG_SIZE) { "VarLong too big" }
        } while (currentByte.toInt() and CONTINUATION_BIT_MASK == CONTINUATION_BIT_MASK)

        return result
    }

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