package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyPacketNotRegisteredException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.NettyConfig;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import java.io.Closeable;
import java.io.IOException;
import lombok.Getter;
import lombok.experimental.Delegate;

public abstract class NettyBase implements Closeable {

  private final SurfPacketRegistry packetRegistry;
  @Getter
  private final String name;

  @Delegate
  private final NettyConfig config;

  public NettyBase(SurfCloudConfig config, SurfPacketRegistry packetRegistry, String name) {
    this.packetRegistry = packetRegistry;
    this.name = name;
    this.config = config.connectionConfig.nettyConfig;
  }

  @Override
  public void close() throws IOException {
    // close
  }

  public final void checkPacket(NettyPacket<?> packet) {
    if (!packetRegistry.isRegistered(packet)) {
      throw new SurfNettyPacketNotRegisteredException("Packet " + packet.getClass().getSimpleName() + " is not registered");
    }
  }
}
