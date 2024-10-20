package dev.slne.surf.cloud.core.netty.protocol.packets.server;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;

@SurfNettyPacket(id = DefaultIds.CLIENT_JOIN)
public class ClientJoinNettyPacket extends NettyPacket<ClientJoinNettyPacket> {

  @Override
  public ClientJoinNettyPacket decode(SurfByteBuf buffer) {
    return this;
  }

  @Override
  public void encode(SurfByteBuf buffer) {

  }
}
