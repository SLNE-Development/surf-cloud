package dev.slne.surf.cloud.core.netty.client;

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
