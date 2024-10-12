package dev.slne.surf.cloud.core.netty.protocol.packets.server;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;

@SurfNettyPacket(id = DefaultIds.KEEP_ALIVE_PACKET)
public class KeepAlivePacket extends NettyPacket<KeepAlivePacket> {

  private long id;

  @Override
  public KeepAlivePacket decode(SurfByteBuf buffer) {
  }

  @Override
  public void encode(SurfByteBuf buffer) {

  }
}
