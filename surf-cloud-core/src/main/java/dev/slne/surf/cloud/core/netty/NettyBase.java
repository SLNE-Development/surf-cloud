package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyPacketNotRegisteredException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.NettyConfig;
import dev.slne.surf.cloud.core.netty.common.registry.listener.SurfNettyListenerRegistry;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.Nullable;

@Flogger
public abstract class NettyBase<C extends NettyContainer<?, ?, ?>> implements AutoCloseable {

  private final SurfPacketRegistry packetRegistry;
  private final SurfNettyListenerRegistry listenerRegistry;
  @Getter
  @Accessors(fluent = true)
  private final String name;

  @Delegate
  private final NettyConfig config;

  @Getter
  @Accessors(fluent = true)
  private C container;

  public NettyBase(
      SurfCloudConfig config,
      SurfPacketRegistry packetRegistry,
      SurfNettyListenerRegistry listenerRegistry,
      String name
  ) {
    this.packetRegistry = packetRegistry;
    this.listenerRegistry = listenerRegistry;
    this.name = name;
    this.config = config.connectionConfig.nettyConfig;
  }

  protected abstract C createContainer();

  @OverridingMethodsMustInvokeSuper
  @PostConstruct
  public void connect() {
    this.container = createContainer();
    container.start().join();
  }

  @Override
//  @PreDestroy
  public void close() throws Exception {
    container.close();
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
      NettySource source,
      @Nullable ProxiedNettySource proxiedSource
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
