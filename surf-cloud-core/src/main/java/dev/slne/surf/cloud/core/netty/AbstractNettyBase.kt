package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import dev.slne.surf.cloud.api.exceptions.FatalSurfError.ExitCodes;
import dev.slne.surf.cloud.api.netty.NettyBase;
import dev.slne.surf.cloud.api.netty.exception.SurfNettyPacketNotRegisteredException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.source.NettySource;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.NettyConfig;
import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection;
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry;
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo;
import jakarta.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Flogger
@Accessors(fluent = true)
@ParametersAreNonnullByDefault
public abstract class AbstractNettyBase<
    SELF extends AbstractNettyBase<SELF, Connection, Client>,
    Connection extends AbstractNettyConnection<Connection, Client, SELF>,
    Client extends ProxiedNettySource<Client>
    > implements Closeable, NettyBase<Client> {

  private final NettyPacketRegistry packetRegistry;
  private final NettyListenerRegistry listenerRegistry;

  @Getter
  private final String name;

  @Delegate
  private final NettyConfig config;

  @Getter
  private final ConfigurableApplicationContext context;

  @Getter
  private Connection connection;

  public AbstractNettyBase(
      SurfCloudConfig config,
      NettyPacketRegistry packetRegistry,
      NettyListenerRegistry listenerRegistry,
      String name, ConfigurableApplicationContext context
  ) {
    this.packetRegistry = packetRegistry;
    this.listenerRegistry = listenerRegistry;
    this.name = name;
    this.config = config.connectionConfig.nettyConfig;
    this.context = context;
  }

  protected abstract Connection createContainer();

  @OverridingMethodsMustInvokeSuper
  @PostConstruct
  public void connect() {
    this.connection = createContainer();
  }

  @EventListener
  public void handleContextRefreshedEvent(ContextRefreshedEvent ignored) {
    try {
      connection.tryEstablishConnection();
    } catch (Exception e) {
      throw FatalSurfError.builder()
          .simpleErrorMessage("Failed to establish connection to the server.")
          .detailedErrorMessage("An error occurred while trying to connect to " + host() + ":" + port() + ".")
          .cause(e)
          .additionalInformation("Check if the server is online and reachable.")
          .additionalInformation("Ensure the network configuration is correct.")
          .possibleSolution("Verify the host and port settings.")
          .possibleSolution("Check firewall settings or network issues.")
          .exitCode(ExitCodes.CONNECTION_FAILURE)
          .build();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      connection.close();
    } catch (Exception e) {
      throw new IOException("Failed to close container", e);
    }
  }

  public final void checkPacket(NettyPacket<?> packet) {
    if (!packetRegistry.isRegistered(packet)) {
      throw new SurfNettyPacketNotRegisteredException(
          "Packet " + packet.getClass().getSimpleName() + " is not registered");
    }
  }

  public final <T extends NettyPacket<T>> T createPacket(int packetId) {
    return packetRegistry.createPacket(packetId);
  }

  public final void onPacketReceived(
      NettyPacket<?> packet,
      NettySource<?> source,
      @Nullable ProxiedNettySource<?> proxiedSource
  ) {
    try {
      listenerRegistry.callListeners(packet, new NettyPacketInfo(source, proxiedSource));
    } catch (Exception e) {
      log.atWarning()
          .withCause(e)
          .atMostEvery(15, TimeUnit.SECONDS)
          .log("Failed to call listeners for packet %s", packet.getClass().getSimpleName());
    }
  }
}
