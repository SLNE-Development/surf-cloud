package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkEncoded
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeVarInt
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import java.nio.charset.StandardCharsets

/**
 * Utility object for handling UTF-8 encoded strings in a [ByteBuf].
 *
 * This utility provides methods for reading and writing UTF-8 strings, ensuring that string lengths
 * and encoding constraints are enforced to prevent errors or buffer overflows.
 */
object Utf8String {
    val STREAM_CODEC = streamCodec<ByteBuf, String>(
        { buf, string -> write(buf, string, Int.MAX_VALUE) },
        { buf -> read(buf, Int.MAX_VALUE) }
    )

    /**
     * Reads a UTF-8 encoded string from the specified [ByteBuf].
     *
     * The string is preceded by its length, encoded as a VarInt.
     * The method ensures that the
     * length does not exceed the maximum allowable length
     * and that the buffer contains sufficient
     * data to decode the string.
     *
     * @param buf The [ByteBuf] to read the string from.
     * @param maxLength The maximum allowable character length of the decoded string.
     * @return The decoded string.
     * @throws IllegalArgumentException If the string's UTF-8 length exceeds the maximum allowed length
     *                                  or is inconsistent with the buffer's readable bytes.
     */
    fun read(buf: ByteBuf, maxLength: Int): String {
        val utf8MaxLength = ByteBufUtil.utf8MaxBytes(maxLength)
        val utf8Length = buf.readVarInt()

        checkDecoded(utf8Length <= utf8MaxLength) { "String too long (max $maxLength): $utf8Length" }
        checkDecoded(utf8Length >= 0) { "String length is negative: $utf8Length" }

        val readableBytes = buf.readableBytes()

        checkDecoded(utf8Length <= readableBytes) { "String length is longer than readable bytes: $utf8Length > $readableBytes" }

        val string = buf.toString(buf.readerIndex(), utf8Length, StandardCharsets.UTF_8)
        buf.readerIndex(buf.readerIndex() + utf8Length)

        checkDecoded(string.length <= maxLength) { "String too long (max $maxLength): ${string.length}" }

        return string
    }

    /**
     * Writes a UTF-8 encoded string to the specified [ByteBuf].
     *
     * The string's length is written first as a VarInt, followed by the UTF-8 encoded bytes of the string.
     * The method validates that the string length does not exceed the specified maximum length.
     *
     * @param buf The [ByteBuf] to write the string to.
     * @param string The string to encode and write.
     * @param maxLength The maximum allowable character length of the string.
     * @throws IllegalArgumentException If the string's length exceeds the specified maximum length.
     */
    fun write(buf: ByteBuf, string: CharSequence, maxLength: Int) {
        checkEncoded(string.length <= maxLength) { "String too long (max $maxLength): ${string.length}" }

        val utf8Length = ByteBufUtil.utf8MaxBytes(string)
        val utf8Buffer = buf.alloc().buffer(utf8Length)

        try {
            val bytesWritten = ByteBufUtil.writeUtf8(utf8Buffer, string)
            val utf8MaxLength = ByteBufUtil.utf8MaxBytes(maxLength)

            checkEncoded(bytesWritten <= utf8MaxLength) { "String too long (max $maxLength): $bytesWritten" }

            buf.writeVarInt(bytesWritten)
            buf.writeBytes(utf8Buffer)
        } finally {
            utf8Buffer.release()
        }
    }
}

