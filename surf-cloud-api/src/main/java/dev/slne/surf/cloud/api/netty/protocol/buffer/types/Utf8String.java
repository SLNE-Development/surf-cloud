package dev.slne.surf.cloud.api.netty.protocol.buffer.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;

/**
 * The type Utf 8 string.
 */
public final class Utf8String {

  /**
   * Read string.
   *
   * @param buf       the buf
   * @param maxLength the max length
   * @return the string
   */
  public static String read(ByteBuf buf, int maxLength) {
    final int utf8MaxLength = ByteBufUtil.utf8MaxBytes(maxLength);
    final int utf8Length = buf.readInt();

    if (utf8Length > utf8MaxLength) {
      throw new DecoderException("String too long (max " + maxLength + "): " + utf8Length);
    }

    if (utf8Length < 0) {
      throw new DecoderException("String length is negative: " + utf8Length);
    }

    final int readableBytes = buf.readableBytes();

    if (utf8Length > readableBytes) {
      throw new DecoderException(
          "String length is longer than readable bytes: " + utf8Length + " > " + readableBytes);
    }

    final String string = buf.toString(buf.readerIndex(), utf8Length, StandardCharsets.UTF_8);
    buf.readerIndex(buf.readerIndex() + utf8Length);

    if (string.length() > maxLength) {
      throw new DecoderException("String too long (max " + maxLength + "): " + string.length());
    }

    return string;
  }

  /**
   * Write.
   *
   * @param buf       the buf
   * @param string    the string
   * @param maxLength the max length
   */
  public static void write(ByteBuf buf, CharSequence string, int maxLength) {
    if (string.length() > maxLength) {
      throw new EncoderException("String too long (max " + maxLength + "): " + string.length());
    }

    final int utf8Length = ByteBufUtil.utf8MaxBytes(string);
    final ByteBuf utf8Buffer = buf.alloc().buffer(utf8Length);

    try {
      final int bytesWritten = ByteBufUtil.writeUtf8(utf8Buffer, string);
      final int utf8MaxLength = ByteBufUtil.utf8MaxBytes(maxLength);

      if (bytesWritten > utf8MaxLength) {
        throw new EncoderException("String too long (max " + maxLength + "): " + bytesWritten);
      }

      buf.writeInt(bytesWritten);
      buf.writeBytes(utf8Buffer);
    } finally {
      utf8Buffer.release();
    }
  }
}

