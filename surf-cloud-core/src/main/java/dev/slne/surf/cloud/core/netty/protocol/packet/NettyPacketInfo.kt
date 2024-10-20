package dev.slne.surf.cloud.core.netty.protocol.packet;

import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.api.netty.source.NettyServerSource;
import dev.slne.surf.cloud.api.netty.source.NettySource;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public record NettyPacketInfo(
    NettySource<?> source,
    Optional<ProxiedNettySource<?>> proxiedSource
) {

  public NettyPacketInfo(NettySource<?> source) {
    this(source, Optional.empty());
  }

  public NettyPacketInfo(NettySource<?> source, @Nullable ProxiedNettySource<?> proxiedSource) {
    this(source, Optional.ofNullable(proxiedSource));
  }

  @Contract(pure = true)
  public NettyClientSource asClientSource() {
    return (NettyClientSource) source;
  }

  @Contract(pure = true)
  public NettyServerSource asServerSource() {
    return (NettyServerSource) source;
  }
}
