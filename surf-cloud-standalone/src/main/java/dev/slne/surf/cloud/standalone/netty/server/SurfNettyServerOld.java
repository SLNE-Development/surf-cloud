package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.common.SurfNettyChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("independent")
@Component
public class SurfNettyServerOld extends NettyBase {

  private final ServerBootstrap serverBootstrap;
  private final SurfNettyChannelInitializer surfNettyChannelInitializer;
  private EventLoopGroup parentGroup = new NioEventLoopGroup();
  private EventLoopGroup workerGroup = new NioEventLoopGroup();
  private SocketChannel connectedChannel;

  public SurfNettyServerOld(SurfNettyChannelInitializer surfNettyChannelInitializer) {
    super(name);

    this.serverBootstrap = new ServerBootstrap()
        .option(ChannelOption.AUTO_READ, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .group(parentGroup, workerGroup)
        .childHandler(surfNettyChannelInitializer)
        .channel(NioServerSocketChannel.class);
    this.surfNettyChannelInitializer = surfNettyChannelInitializer;
  }

  @PostConstruct
  public void start() {
    serverBootstrap.bind(new InetSocketAddress("127.0.0.1", 8888))
        .syncUninterruptibly();
  }


  @PreDestroy
  public void close() {
    parentGroup.shutdownGracefully().syncUninterruptibly();
    workerGroup.shutdownGracefully().syncUninterruptibly();
  }
}
