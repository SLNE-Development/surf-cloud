package dev.slne.surf.data.api.netty.protocol.buffer.codec;

import dev.slne.surf.data.api.netty.protocol.buffer.SurfByteBuf;
import org.jetbrains.annotations.NotNull;

public interface Codec<T, DX extends Throwable, EX extends Throwable> {

  static <T, DX extends Throwable, EX extends Throwable> @NotNull Codec<T, DX, EX> codec(
      final @NotNull Encoder<T, EX> encoder,
      final @NotNull Decoder<T, DX> decoder
  ) {
    return new Codec<>() {
      @Override
      public @NotNull T decode(@NotNull SurfByteBuf buf) throws DX {
        return decoder.decode(buf);
      }

      @Override
      public void encode(@NotNull SurfByteBuf buf, @NotNull T value) throws EX {
        encoder.encode(buf, value);
      }
    };
  }

  @NotNull
  T decode(@NotNull SurfByteBuf buf) throws DX;

  void encode(@NotNull SurfByteBuf buf, @NotNull T value) throws EX;

  interface Decoder<D, DX extends Throwable> {

    @NotNull
    D decode(@NotNull SurfByteBuf buf) throws DX;
  }

  interface Encoder<T, EX extends Throwable> {

    void encode(@NotNull SurfByteBuf buf, @NotNull T value) throws EX;
  }
}

