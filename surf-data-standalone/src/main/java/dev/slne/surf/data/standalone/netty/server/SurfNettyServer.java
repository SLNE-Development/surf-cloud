package dev.slne.surf.data.standalone.netty.server;

import dev.slne.surf.data.core.netty.common.SurfNettyChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("standalone")
@Component
public class SurfNettyServer  {

  private final ServerBootstrap serverBootstrap;
  private final SurfNettyChannelInitializer surfNettyChannelInitializer;
  private EventLoopGroup parentGroup = new NioEventLoopGroup();
  private EventLoopGroup workerGroup = new NioEventLoopGroup();
  private SocketChannel connectedChannel;

  public SurfNettyServer(SurfNettyChannelInitializer surfNettyChannelInitializer) {
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
