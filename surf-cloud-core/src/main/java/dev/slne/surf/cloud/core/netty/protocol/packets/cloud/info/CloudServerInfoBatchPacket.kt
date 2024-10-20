package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.server.CloudServer;
import dev.slne.surf.cloud.core.server.CloudServerImpl;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
@Getter
@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_BATCH_PACKET)
public class CloudServerInfoBatchPacket extends NettyPacket<CloudServerInfoBatchPacket> {

  private CloudServer[] servers;
  private CloudServerInfoAction action;

  public CloudServerInfoBatchPacket(CloudServerInfoAction action, CloudServer... servers) {
    this.action = action;
    this.servers = servers;
  }

  public CloudServerInfoBatchPacket(CloudServerInfoAction action, @NotNull Iterable<CloudServer> servers) {
    this(action, StreamSupport.stream(servers.spliterator(), false).toArray(CloudServer[]::new));
  }

  @Override
  public void encode(SurfByteBuf buffer) {
    buffer.writeEnum(action);
    buffer.writeCodecArray(CloudServerImpl.CODEC, servers);
  }

  @Override
  public CloudServerInfoBatchPacket decode(SurfByteBuf buffer) {
    action = buffer.readEnum(CloudServerInfoAction.class);
    servers = buffer.readCodecArray(CloudServerImpl.CODEC, CloudServer[]::new);
    return this;
  }
}
