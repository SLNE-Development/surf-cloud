package dev.slne.surf.cloud.core.netty.client;

import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.NettyContainer;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("client")
public class SurfNettyClient extends NettyBase<NettyClientContainer> {

  public SurfNettyClient(SurfCloudConfig config, SurfPacketRegistry packetRegistry) {
    super(config, packetRegistry, "client"); // TODO: 14.09.2024 19:47 - better name
  }

  @Override
  protected NettyClientContainer createContainer() {
    return new NettyClientContainer(this, getPort(), getHost());
  }
}
