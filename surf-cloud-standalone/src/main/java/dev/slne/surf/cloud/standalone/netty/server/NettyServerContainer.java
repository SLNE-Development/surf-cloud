package dev.slne.surf.cloud.standalone.netty.server;

import com.google.common.flogger.StackSize;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.netty.NettyContainer;
import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.client.NettyClientSource;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import dev.slne.surf.cloud.core.netty.common.SurfNettyChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.flogger.Flogger;

@Getter
@Accessors(fluent = true)
@Flogger
public class NettyServerContainer extends
    NettyContainer<NettyServerContainer, NettyClientSource, SurfNettyServer> {

  private final ServerSourceList serverList;
  private final SurfNettyChannelInitializer channelInitializer;
  private final int port;
  private final EventLoopGroup producer = new NioEventLoopGroup();
  private final EventLoopGroup consumer = new NioEventLoopGroup();

  private ServerBootstrap bootstrap;
  private Channel channel;

  public NettyServerContainer(SurfNettyServer base, SurfNettyChannelInitializer channelInitializer,
      int port) {
    super(base);

    this.serverList = new ServerSourceList(base);
    this.channelInitializer = channelInitializer;
    this.port = port;
  }

  @Override
  public CompletableFuture<Void> start() {
    final CompletableFuture<Void> future = new CompletableFuture<>();

    try {
      this.bootstrap = new ServerBootstrap()
          .group(producer, consumer)
          .channel(NioServerSocketChannel.class)
          .childHandler(channelInitializer)
          .option(ChannelOption.SO_KEEPALIVE, true);

      bootstrap.bind(port).addListener((ChannelFutureListener) channelFuture -> {
        if (channelFuture.isSuccess()) {
          this.channel = channelFuture.channel();
          future.complete(null);
        } else {
          future.completeExceptionally(channelFuture.cause());
        }
      });
    } catch (Exception e) {
      future.completeExceptionally(e);
    }

    return future;
  }

  @Override
  public void close() throws Exception {
    producer.shutdownGracefully().syncUninterruptibly();
    consumer.shutdownGracefully().syncUninterruptibly();
    channel.close().syncUninterruptibly();
  }

  @Override
  public SourceList<NettyClientSource> sourceList() {
    return serverList;
  }

  @Override
  public Optional<? extends NettySource> source(Channel channel) {
    return serverList().client(channel);
  }

  @Override
  public void broadcast(NettyPacket<?> packet) {
    for (final NettyClientSource client : serverList().clients()) {
      sendPacket(client, packet);
    }
  }

  @Override
  protected void sendPacket0(NettySource source, NettyPacket<?> packet) {
    if (!(source instanceof NettyClientSource client)) {
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
}
