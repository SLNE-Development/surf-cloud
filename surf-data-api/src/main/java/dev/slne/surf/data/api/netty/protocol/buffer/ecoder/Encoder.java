package dev.slne.surf.data.api.netty.protocol.buffer.ecoder;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface Encoder<B extends ByteBuf> {

  void encode(B buffer);
}
