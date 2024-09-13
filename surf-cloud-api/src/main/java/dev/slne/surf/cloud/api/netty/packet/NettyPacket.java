package dev.slne.surf.cloud.api.netty.packet;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.Decoder;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.Encoder;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;

@Getter
public abstract class NettyPacket<SELF extends NettyPacket<SELF>> implements
    Decoder<SurfByteBuf, SELF>, Encoder<SurfByteBuf> {

  @Setter
  private long sessionId = ThreadLocalRandom.current().nextLong();

  private final int id;

  public NettyPacket() {
    this.id = AnnotationUtils.findAnnotation(getClass(), SurfNettyPacket.class).id();
  }
}
