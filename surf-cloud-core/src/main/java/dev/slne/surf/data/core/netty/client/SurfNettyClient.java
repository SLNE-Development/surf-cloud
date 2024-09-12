package dev.slne.surf.data.core.netty.client;

import dev.slne.surf.data.core.netty.common.SurfPacketRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("client")
public class SurfNettyClient {

  private final Bootstrap bootstrap;
  private final EventRegistry eventRegistry;

  private final EventLoopGroup workerGroup = new NioEventLoopGroup();

}
