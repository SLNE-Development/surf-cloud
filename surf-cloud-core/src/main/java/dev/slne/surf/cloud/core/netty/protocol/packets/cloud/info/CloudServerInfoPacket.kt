package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.server.CloudServer;
import dev.slne.surf.cloud.core.server.CloudServerImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_PACKET)
public class CloudServerInfoPacket extends NettyPacket<CloudServerInfoPacket> {

  private CloudServerInfoAction action;
  private CloudServer server;

  @Override
  public void encode(SurfByteBuf buffer) {
    buffer.writeWithCodec(CloudServerImpl.CODEC, server);
    buffer.writeEnum(action);
  }

  @Override
  public CloudServerInfoPacket decode(SurfByteBuf buffer) {
    server = buffer.readWithCodec(CloudServerImpl.CODEC);
    action = buffer.readEnum(CloudServerInfoAction.class);
    return this;
  }
}
