package dev.slne.surf.data.api.netty;

import dev.slne.surf.data.api.netty.protocol.buffer.codec.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public interface ServerInfo {

  static Codec<ServerInfo, DecoderException, EncoderException>
}
