package dev.slne.surf.cloud.core.netty.client;

import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.common.registry.listener.SurfNettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("client")
public class SurfNettyClient extends NettyBase<NettyClientContainer> {

  public SurfNettyClient(SurfCloudConfig config, SurfPacketRegistry packetRegistry, SurfNettyListenerRegistry listenerRegistry) {
    super(config, packetRegistry, listenerRegistry, "client"); // TODO: 14.09.2024 19:47 - better name
  }

  @Override
  protected NettyClientContainer createContainer() {
    return new NettyClientContainer(this, port(), host());
  }
}
