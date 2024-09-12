package dev.slne.surf.cloud.api.netty.protocol.buffer.decoder;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface Decoder<B extends ByteBuf, T> {

  T decode(B buffer);
}
