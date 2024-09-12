package dev.slne.surf.cloud.api.netty;

import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public interface ServerInfo {

  static Codec<ServerInfo, DecoderException, EncoderException>
}
