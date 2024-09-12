package dev.slne.surf.cloud.api.netty.packet;

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.Decoder;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.Encoder;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;

public abstract class NettyPacket<SELF extends NettyPacket<SELF>> implements
    Decoder<SurfByteBuf, SELF>, Encoder<SurfByteBuf> {

  @Getter
  @Setter
  private long sessionId = ThreadLocalRandom.current().nextLong();

  public NettyPacket() {

  }
}
