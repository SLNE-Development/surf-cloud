package dev.slne.surf.cloud.core.netty.protocol.packets.cloud;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;

public class CloudServerInfoNettyPacket extends NettyPacket<CloudServerInfoNettyPacket> {

  @Override
  public CloudServerInfoNettyPacket decode(SurfByteBuf buffer) {
    return null;
  }

  @Override
  public void encode(SurfByteBuf buffer) {

  }
}
