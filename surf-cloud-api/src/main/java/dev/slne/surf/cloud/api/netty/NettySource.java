package dev.slne.surf.cloud.api.netty;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class NettySource {

  @Getter
  private final NettyBase nettyBase;

  NettySource(NettyBase nettyBase) {
    this.nettyBase = nettyBase;
  }

  public void sendPacket(NettyPacket<?> packet) {
    // send packet
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
