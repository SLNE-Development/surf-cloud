package dev.slne.surf.cloud.api.netty.packet;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.Decoder;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.Encoder;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.annotation.AnnotationUtils;

@Getter
@Accessors(fluent = true)
public abstract class NettyPacket<SELF extends NettyPacket<SELF>> implements Encoder<SurfByteBuf>, Decoder<SurfByteBuf, SELF> {

  @Setter
  private long sessionId = ThreadLocalRandom.current().nextLong();

  private final int id;

  public NettyPacket() {
    final SurfNettyPacket meta = AnnotationUtils.findAnnotation(getClass(), SurfNettyPacket.class);

    if (meta == null) {
      throw new IllegalArgumentException("NettyPacket class must be annotated with SurfNettyPacket");
    }

    this.id = meta.id();
  }

  // TODO: 16.09.2024 14:49 - send method
}
