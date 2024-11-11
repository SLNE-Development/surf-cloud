package dev.slne.surf.cloud.api.common.netty.protocol.buffer.types

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkEncoded
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeVarInt
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import java.nio.charset.StandardCharsets

/** Utility object for reading and writing UTF-8 encoded strings to a ByteBuf. **/
object Utf8String {

    /**
     * Reads a UTF-8 encoded string from the provided ByteBuf.
     *
     * @param buf The ByteBuf to read the string from.
     * @param maxLength The maximum allowed length of the decoded string.
     * @return The decoded string.
     * @throws IllegalArgumentException if the UTF-8 length exceeds the allowed maximum or if it is negative.
     */
    @JvmStatic
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
     * Writes a UTF-8 encoded string to the provided ByteBuf.
     *
     * @param buf The ByteBuf to write the string to.
     * @param string The string to be written.
     * @param maxLength The maximum allowed length of the string.
     * @throws IllegalArgumentException if the string length exceeds the allowed maximum.
     */
    @JvmStatic
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

