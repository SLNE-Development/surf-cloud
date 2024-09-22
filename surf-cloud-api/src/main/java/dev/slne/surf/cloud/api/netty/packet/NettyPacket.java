package dev.slne.surf.cloud.api.netty.packet;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.Decoder;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.Encoder;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NettyPacket<?> that)) {
      return false;
    }

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  // TODO: 16.09.2024 14:49 - send method
}
