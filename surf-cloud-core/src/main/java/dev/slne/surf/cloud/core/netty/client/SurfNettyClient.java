package dev.slne.surf.cloud.core.netty.client;

import dev.slne.surf.cloud.api.netty.NettyClient;
import dev.slne.surf.cloud.api.netty.source.NettyServerSource;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.client.connection.NettyClientConnectionImpl;
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Component
@Profile("client")
@Accessors(fluent = true)
public class SurfNettyClient extends
    AbstractNettyBase<SurfNettyClient, NettyClientConnectionImpl, NettyServerSource> implements
    NettyClient {

  public SurfNettyClient(
      SurfCloudConfig config,
      NettyPacketRegistry packetRegistry,
      NettyListenerRegistry listenerRegistry,
      ConfigurableApplicationContext context
  ) {
    super(config, packetRegistry, listenerRegistry,
        "client", context); // TODO: 14.09.2024 19:47 - better name
  }

  @Override
  protected NettyClientConnectionImpl createConnectionInstance() {
    return new NettyClientConnectionImpl(this);
  }
}
