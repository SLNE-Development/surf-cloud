package dev.slne.surf.cloud.core.netty.client;

import dev.slne.surf.cloud.api.config.properties.CloudProperties;
import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.data.CloudPersistentData;
import dev.slne.surf.cloud.core.netty.NettyContainer;
import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.common.ReconnectHandler;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket.Type;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudServerRegistrationData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.flogger.Flogger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Accessors(fluent = true)
@Flogger
public class NettyClientContainer extends
    NettyContainer<NettyClientContainer, ProxiedNettySource, SurfNettyClient> {

  private final int port;
  private final String host;
  private CompletableFuture<Void> connectFuture;


  private final SourceList<ProxiedNettySource> sourceList = new SourceList<>(base());
  private final EventLoopGroup group = new NioEventLoopGroup();
  private final ReconnectHandler reconnect = new ReconnectHandler(this, Duration.ofSeconds(5));
  private Bootstrap bootstrap;
  private NettySource source;
  private Channel channel;
  /**
   * -- GETTER -- Gets the after executing
   *
   * @return the {@link ProxiedNettySource}
   */
  @Getter
  private ProxiedNettySource serverSource;

  public NettyClientContainer(SurfNettyClient client, int port, String host) {
    super(client);
    this.port = port;
    this.host = host;
  }

  @Override
  public CompletableFuture<Void> start() {
    if (connectFuture != null) {
      return connectFuture;
    }
    connectFuture = new CompletableFuture<>();
    reconnect.requestReconnect();

    this.bootstrap = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
//        .handler(new PacketChannelInitializer(base()))
        .option(ChannelOption.SO_KEEPALIVE, true);

    this.bootstrap.connect(this.host, this.port).addListener((ChannelFutureListener) (future -> {
      if (!future.isSuccess()) {
        reconnect.scheduleReconnect(group.next());
        return;
      }

      this.source = new NettySource(base());
      this.channel = future.channel();
      this.channel.close().addListener(reconnect);
      log.atInfo()
          .log("Connected to %s", this.channel.remoteAddress());
      connectFuture.complete(null);
      connectFuture = null;
//      base.handlePacketRead(new PacketPostConnected(), source, null); // TODO registration packet
    }));

    return connectFuture;
  }

  private void sendFetchPreload() {
    registerServer(new CloudServerRegistrationData(
        CloudPersistentData.SERVER_ID.value(),
        CloudProperties.SERVER_CATEGORY.value(),
        port(),
        host()
    )).thenAcceptAsync(source -> {
      CloudPersistentData.SERVER_ID.setValue(source.serverGuid());
    });
  }

  @Override
  public void close() throws Exception {
    reconnect.requestDisconnect();
    if (this.channel != null) {
      this.channel.close().syncUninterruptibly();
    }
    group.shutdownGracefully().syncUninterruptibly();
  }

  @Override
  public boolean connected() {
    return this.channel != null && this.channel.isActive();
  }

  // TODO: 14.09.2024 18:35 - https://git.slne.dev/silence/creepcore/-/blob/master/CreepBungee/src/main/java/me/tomicake/capi/bungee/client/ContainerClient.java?ref_type=heads#L129
  public CompletableFuture<ProxiedNettySource> registerServer(CloudServerRegistrationData data) {
    final CompletableFuture<ProxiedNettySource> future = new CompletableFuture<>();
    final CloudRegisterServerPacket packet = new CloudRegisterServerPacket(Type.FETCH_PRELOAD, data);
    this.sendPacket(packet);

    future.complete(sourceList().findByServerGuid(data.serverId()).orElseThrow());

    return future;
//    sourceList().findByServerGuid()
  }

  @Override
  public SourceList<ProxiedNettySource> sourceList() {
    return sourceList;
  }

  @Override
  public Optional<NettySource> source(Channel channel) {
    return Optional.ofNullable(source);
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
      final ProxiedNettyPacket proxied = new ProxiedNettyPacket(packet, proxiedSource, base());
      channel.writeAndFlush(proxied);
    } else {
      channel.writeAndFlush(packet);
    }
  }

  @Override
  public void broadcast(NettyPacket<?> packet) {
    sendPacket(packet);
  }

  @Component
  @Profile("client")
  static class InternalPacketListener {

    @SurfNettyPacketHandler
    public void onCloudRegisterServer(CloudRegisterServerPacket packet) {
      final long serverId = packet.getData().serverId();
    }
  }
}
