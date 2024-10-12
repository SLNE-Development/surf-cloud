package dev.slne.surf.cloud.standalone.netty.server.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.StackSize;
import dev.slne.surf.cloud.api.netty.connection.NettyServerConnection;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl;
import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection;
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource;
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier;
import dev.slne.surf.cloud.core.netty.protocol.channel.SurfNettyChannelInitializer;
import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer;
import dev.slne.surf.cloud.standalone.netty.server.source.tracker.ServerNettyClientTrackerImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.flogger.Flogger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Accessors(fluent = true)
@Flogger
@ParametersAreNonnullByDefault
public class NettyServerConnectionImpl extends
    AbstractNettyConnection<NettyServerConnectionImpl, NettyClientSource, SurfNettyServer> implements
    NettyServerConnection {

  private final int port;
  private final ServerNettyClientTrackerImpl clientTracker;
  private final ConfigurableApplicationContext context;

  private final EventLoopGroup bossGroup = new NioEventLoopGroup();
  private final EventLoopGroup workerGroup = new NioEventLoopGroup();

  private ServerChannel channel;

  public NettyServerConnectionImpl(SurfNettyServer server) {
    super(server);

    this.port = server.port();
    this.clientTracker = new ServerNettyClientTrackerImpl(server);
    this.context = server.context();
  }

  @Override
  protected void tryEstablishConnection0() throws Exception {
    log.atInfo()
        .log("Starting server on port %d", port);

    new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(context.getBean(SurfNettyChannelInitializer.class))
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .bind(port)
        .sync()
        .addListener((ChannelFutureListener) listener -> {
          if (listener.isSuccess()) {
            this.channel = (ServerChannel) listener.channel();

            log.atInfo()
                .log("Server started on port %d", port);
          } else {
            throw new IllegalStateException("Failed to bind to port " + port, listener.cause());
          }
        });
  }

  @Override
  public void close() throws Exception {
    channel.close().sync();
    bossGroup.shutdownGracefully().sync();
    workerGroup.shutdownGracefully().sync();
  }

  @Override
  public ServerNettyClientTrackerImpl clientTracker() {
    return clientTracker;
  }

  @Override
  public Optional<? extends NettyClientSource> source(Channel channel) {
    checkNotNull(channel, "channel");

    return clientTracker().client(channel);
  }

  @Override
  public void broadcast(NettyPacket<?> packet) {
    for (final NettyClientSource client : clientTracker().clients()) {
      sendPacket(client, packet);
    }
  }

  @Override
  protected void sendPacket0(AbstractNettySource<NettyClientSource> source, NettyPacket<?> packet) {
    if (!(source instanceof NettyClientSourceImpl client)) {
      log.atWarning()
          .withStackTrace(StackSize.MEDIUM)
          .atMostEvery(10, TimeUnit.SECONDS)
          .log("Source %s is not a client", source);
      return;
    }

    client.channel().writeAndFlush(packet);
  }

  @Override
  public boolean connected() {
    return channel != null && channel.isActive();
  }

  @Component
  @Profile("independent")
  public static class NettyServerChannelInitializerModifier implements ChannelInitializerModifier {

    @Override
    public void modify(SocketChannel ch) throws Exception {
      System.err.println("MODIFYING CHANNEL");
//      ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
    }
  }
}
