package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.api.netty.NettyServer;
import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry;
import dev.slne.surf.cloud.standalone.netty.server.connection.NettyServerConnectionImpl;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Component
@Profile("independent")
@Accessors(fluent = true)
public class SurfNettyServer extends
    AbstractNettyBase<SurfNettyServer, NettyServerConnectionImpl, NettyClientSource> implements
    NettyServer {

  public SurfNettyServer(
      SurfCloudConfig config,
      NettyPacketRegistry packetRegistry,
      NettyListenerRegistry listenerRegistry,
      ConfigurableApplicationContext context
  ) {
    super(config, packetRegistry, listenerRegistry,
        "independent", context); // TODO: 14.09.2024 19:47 - better name
  }

  @Override
  protected NettyServerConnectionImpl createConnectionInstance() {
    return new NettyServerConnectionImpl(this);
  }
}
