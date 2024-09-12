package dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder;

import io.netty.buffer.ByteBuf;

/**
 * The interface Encode factory.
 *
 * @param <B> the type parameter
 * @param <T> the type parameter
 */
@FunctionalInterface
public interface EncodeFactory<B extends ByteBuf, T> {

  /**
   * Encode.
   *
   * @param buffer the buffer
   * @param value  the value
   */
  void encode(B buffer, T value);

  @FunctionalInterface
  interface EncodeLongFactory<B extends ByteBuf> extends EncodeFactory<B, Long> {

    @Deprecated
    default void encode(B buffer, Long value) {
      encodeLong(buffer, value);
    }

    void encodeLong(B buffer, long value);
  }
}

