package dev.slne.surf.cloud.api.netty.protocol.buffer.decoder;

import io.netty.buffer.ByteBuf;

/**
 * The interface Decode factory.
 *
 * @param <B> the type parameter
 * @param <T> the type parameter
 */
@FunctionalInterface
public interface DecodeFactory<B extends ByteBuf, T> {

  /**
   * Decode t.
   *
   * @param buffer the buffer
   * @return the t
   */
  T decode(B buffer);

  /**
   * The interface Decode builder factory.
   *
   * @param <B> the type parameter
   * @param <T> the type parameter
   */
  @FunctionalInterface
  interface DecodeBuilderFactory<B extends ByteBuf, T> {

    /**
     * Decode t.
     *
     * @param buffer the buffer
     * @param object the object
     * @return the t
     */
    T decode(B buffer, T object);
  }

  @FunctionalInterface
  interface DecodeLongFactory<B extends ByteBuf> extends DecodeFactory<B, Long> {

    @Deprecated
    default Long decode(B buffer) {
      return decodeLong(buffer);
    }

    long decodeLong(B buffer);
  }
}