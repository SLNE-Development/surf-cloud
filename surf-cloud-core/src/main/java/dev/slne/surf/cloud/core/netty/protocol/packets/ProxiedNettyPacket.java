package dev.slne.surf.cloud.core.netty.protocol.packets;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.client.SurfNettyClient;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@SurfNettyPacket(id = DefaultIds.PROXIED_NETTY_PACKET)
@Accessors(fluent = true)
public class ProxiedNettyPacket extends NettyPacket<ProxiedNettyPacket> {

  private final SurfNettyClient client;
  private NettyPacket<?> packet;
  private ProxiedNettySource source;
  private ProxiedNettySource target;

  public ProxiedNettyPacket() {
    this.client = SurfCloudCoreInstance.get().getDataContext().getBean(SurfNettyClient.class);
  }

  public ProxiedNettyPacket(NettyPacket<?> packet, ProxiedNettySource target, SurfNettyClient client) {
    this.packet = packet;
    this.target = target;
    this.source = client.container().serverSource();
    this.client = client;
  }

  @Override
  public void encode(SurfByteBuf buffer) {
    buffer.writeLong(target.serverGuid());
    buffer.writeLong(source.serverGuid());
    packet.encode(buffer);
  }

  @Override
  public ProxiedNettyPacket decode(SurfByteBuf buffer) {
    final long targetGuid = buffer.readLong();
    final long sourceGuid = buffer.readLong();
    final int packetId = buffer.readInt();

    final SourceList<ProxiedNettySource> sourceList = client.container().sourceList();
    this.target = sourceList.findByServerGuid(targetGuid).orElseThrow();
    this.source = sourceList.findByServerGuid(sourceGuid).orElseThrow();

    this.packet = client.createPacket(packetId);
    packet.decode(buffer);

    return this;
  }
}
