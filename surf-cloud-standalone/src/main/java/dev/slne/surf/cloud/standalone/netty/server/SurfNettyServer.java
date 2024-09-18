package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.common.SurfNettyChannelInitializer;
import dev.slne.surf.cloud.core.netty.common.registry.listener.SurfNettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("independent")
public class SurfNettyServer extends NettyBase<NettyServerContainer> {

  private final SurfNettyChannelInitializer surfNettyChannelInitializer;

  public SurfNettyServer(
      SurfCloudConfig config,
      SurfPacketRegistry packetRegistry,
      SurfNettyListenerRegistry listenerRegistry,
      SurfNettyChannelInitializer surfNettyChannelInitializer
  ) {
    super(config, packetRegistry, listenerRegistry,
        "independent"); // TODO: 14.09.2024 19:47 - better name
    this.surfNettyChannelInitializer = surfNettyChannelInitializer;
  }

  @Override
  protected NettyServerContainer createContainer() {
    return new NettyServerContainer(this, surfNettyChannelInitializer, port());
  }
}
