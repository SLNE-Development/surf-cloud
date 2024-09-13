package dev.slne.surf.cloud.core.netty.client;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.NettyContainer;
import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.flogger.Flogger;

@Getter
@Accessors(fluent = true)
@Flogger
public class NettyClientContainer extends NettyContainer<ProxiedNettySource> {

  private final int port;
  private final String host;
  private CompletableFuture<Void> connectFuture;


  private final SourceList<ProxiedNettySource> sourceList = new SourceList<>(base());
  private final EventLoopGroup group = new NioEventLoopGroup();
  private Bootstrap bootstrap;
  private NettySource source;
  private Channel channel;
  /**
   * -- GETTER --
   *  Gets the
   *  after executing
   *
   * @return the {@link ProxiedSource}
   */
  @Getter
  private ProxiedNettySource serverSource;

  public NettyClientContainer(NettyBase nettyBase, int port, String host) {
    super(nettyBase);
    this.port = port;
    this.host = host;
  }

  @Override
  public CompletableFuture<Void> start() {
    if (connectFuture != null) {
      return connectFuture;
    }
    connectFuture = new CompletableFuture<>();
    // reconnect.requestReconnect();

    this.bootstrap = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
//        .handler(new PacketChannelInitializer(base()))
        .option(ChannelOption.SO_KEEPALIVE, true);

    this.bootstrap.connect(this.host, this.port).addListener((ChannelFutureListener) (future -> {
      if (!future.isSuccess()) {
//        reconnect.scheduleReconnect(); // TODO
        return;
      }

      this.source = new NettySource(base());
      this.channel = future.channel();
//      this.channel.close().addListener(reconnect);
      log.atInfo()
          .log("Connected to %s", this.channel.remoteAddress());
      connectFuture.complete(null);
      connectFuture = null;
//      base.handlePacketRead(new PacketPostConnected(), source, null); // TODO registration packet
    }));

    return connectFuture;
  }

  @Override
  public void close() throws Exception {
//    reconnect.requestDisconnect();
    if (this.channel != null) {
      this.channel.close().syncUninterruptibly();
    }
    group.shutdownGracefully().syncUninterruptibly();
  }

  @Override
  public boolean connected() {
    return this.channel != null && this.channel.isActive();
  }

//  public CompletableFuture<ProxiedSource> registerServer(ServerRegistrationData data) {}

  @Override
  public SourceList<ProxiedNettySource> sourceList() {
    return sourceList;
  }

  @Override
  public NettySource source(Channel channel) {
    return source;
  }

  /**
   * Sends a packet with the local source
   *
   * @param packet the packet to send
   */
  public void sendPacket(NettyPacket<?> packet) {
    this.sendPacket(source(), packet);
  }

  @Override
  protected void sendPacket0(NettySource source, NettyPacket<?> packet) {
    if (source instanceof ProxiedNettySource proxiedSource) {
      PacketProxied proxied = new PacketProxied(packet, proxiedSource, base());
      channel.writeAndFlush(proxied);
    } else {
      channel.writeAndFlush(packet);
    }

  }

  @Override
  public void broadcast(NettyPacket<?> packet) {
    sendPacket(packet);
  }

  @Override
  public NettyBase base() {
    return null;
  }
}
