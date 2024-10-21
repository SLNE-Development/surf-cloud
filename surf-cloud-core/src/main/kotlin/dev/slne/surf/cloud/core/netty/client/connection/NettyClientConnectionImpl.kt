package dev.slne.surf.cloud.core.netty.client.connection;

import dev.slne.surf.cloud.api.config.properties.CloudProperties;
import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import dev.slne.surf.cloud.api.netty.connection.NettyClientConnection;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.source.NettyServerSource;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.api.netty.source.tracker.NettyClientTracker;
import dev.slne.surf.cloud.core.data.CloudPersistentData;
import dev.slne.surf.cloud.core.netty.client.SurfNettyClient;
import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection;
import dev.slne.surf.cloud.core.netty.common.connection.NettyConnectionRecoveryHandler;
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource;
import dev.slne.surf.cloud.core.netty.common.source.NettyServerSourceImpl;
import dev.slne.surf.cloud.core.netty.common.source.tracker.NettyClientTrackerImpl;
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier;
import dev.slne.surf.cloud.core.netty.protocol.channel.SurfNettyChannelInitializer;
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket.Type;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudServerRegistrationData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
public class NettyClientConnectionImpl extends
    AbstractNettyConnection<NettyClientConnectionImpl, NettyServerSource, SurfNettyClient> implements
    NettyClientConnection {

  private final int port;
  private final String host;

  private final NettyClientTrackerImpl<NettyServerSource> clientTracker = new NettyClientTrackerImpl<>(
      base());
  private final NioEventLoopGroup group = new NioEventLoopGroup(1);
  private final NettyConnectionRecoveryHandler recoveryHandler = new NettyConnectionRecoveryHandler(
      this, Duration.ofSeconds(5));
  private final ConfigurableApplicationContext context;

  private NettyServerSource source;
  private Channel channel;

  @Getter
  private NettyServerSource serverSource;

  public NettyClientConnectionImpl(SurfNettyClient client) {
    super(client);

    this.port = client.port();
    this.host = client.host();
    this.context = client.context();
  }

  @Override
  protected void tryEstablishConnection0() throws Exception {
    log.atInfo()
        .log("Connecting to '%s:%d' ...", this.host, this.port);

    this.recoveryHandler.stopReconnection();

    final Bootstrap bootstrap = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
        .handler(context.getBean(SurfNettyChannelInitializer.class))
        .option(ChannelOption.SO_KEEPALIVE, true);
//        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    final ChannelFuture connected = bootstrap.connect(this.host, this.port)
        .addListener((ChannelFutureListener) (future -> {
          if (!future.isSuccess()) {
            log.atSevere().log("Failed to connect to %s:%d - %s", this.host, this.port,
                future.cause().getMessage());
            this.recoveryHandler.attemptReconnect(group.next());
            return;
          }

          log.atInfo().log("Successfully connected to %s:%d", this.host, this.port);

          this.source = new NettyServerSourceImpl(base());
          this.channel = future.channel();
          this.channel.closeFuture().addListener(recoveryHandler);
//          this.recoveryHandler.resumeReconnect();

          log.atInfo()
              .log("Connected to %s!", this.channel.remoteAddress());
//      base.handlePacketRead(new PacketPostConnected(), source, null); // TODO registration packet
        }))
        .sync();

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
    this.recoveryHandler.stopReconnection();

    if (this.channel != null) {
      this.channel.close().sync();
    }

    this.group.shutdownGracefully().sync();
  }

  @Override
  public boolean connected() {
    return this.channel != null && this.channel.isActive();
  }

  // TODO: 14.09.2024 18:35 - https://git.slne.dev/silence/creepcore/-/blob/master/CreepBungee/src/main/java/me/tomicake/capi/bungee/client/ContainerClient.java?ref_type=heads#L129
  public CompletableFuture<ProxiedNettySource> registerServer(CloudServerRegistrationData data) {
    final CompletableFuture<ProxiedNettySource> future = new CompletableFuture<>();
    final CloudRegisterServerPacket packet = new CloudRegisterServerPacket(Type.FETCH_PRELOAD,
        data);
    this.sendPacket(packet);

    future.complete(clientTracker().findByServerGuid(data.serverId).orElseThrow());

    return future;
//    sourceTracker().findByServerGuid()
  }

  @Override
  public NettyClientTracker<NettyServerSource> clientTracker() {
    return clientTracker;
  }

  @Override
  public Optional<NettyServerSource> source(Channel unused) {
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
  protected void sendPacket0(AbstractNettySource<NettyServerSource> source, NettyPacket<?> packet) {
    if (source instanceof NettyServerSourceImpl nettyServerSource) {
      final ProxiedNettyPacket proxied = new ProxiedNettyPacket(packet, nettyServerSource, base());
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
  public static class InternalPacketListener {

    @SurfNettyPacketHandler
    public void onCloudRegisterServer(CloudRegisterServerPacket packet) {
      final long serverId = packet.getData().serverId;
    }
  }

  @Component
  @Profile("client")
  public static class NettyCLientChannelInitializerModifier implements ChannelInitializerModifier {

    @Override
    public void modify(SocketChannel channel) {
      System.err.println("MODIFYING CHANNEL");
      channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(15, 15, 0))
          .addLast("keepAliveHandler", new NettyKeepAliveHandler());
    }

    private static class NettyKeepAliveHandler extends ChannelDuplexHandler {

      @Override
      public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
          if (event.state().equals(IdleState.WRITER_IDLE)) {
            System.out.println("Sending Keepalive packet...");

          }
        } else {
          super.userEventTriggered(ctx, evt);
        }
      }
    }
  }
}
