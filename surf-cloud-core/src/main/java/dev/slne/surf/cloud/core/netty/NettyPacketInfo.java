package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.core.netty.client.NettyClientSource;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public record NettyPacketInfo(
    NettySource source,
    Optional<ProxiedNettySource> proxiedSource
) {

  public NettyPacketInfo(NettySource source) {
    this(source, Optional.empty());
  }

  public NettyPacketInfo(NettySource source, @Nullable ProxiedNettySource proxiedSource) {
    this(source, Optional.ofNullable(proxiedSource));
  }

  public NettyClientSource clientSource() {
    return (NettyClientSource) source;
  }
}
