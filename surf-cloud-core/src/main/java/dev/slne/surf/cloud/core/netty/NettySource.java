package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@Accessors(fluent = true)
public class NettySource {

  private final NettyBase<?> nettyBase;

  public NettySource(NettyBase<?> nettyBase) {
    this.nettyBase = nettyBase;
  }

  public void sendPacket(NettyPacket<?> packet) {
    // send packet
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NettySource source)) {
      return false;
    }

    return Objects.equals(nettyBase, source.nettyBase);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nettyBase);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
