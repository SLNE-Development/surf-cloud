package dev.slne.surf.cloud.core.netty.common;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyPacketEncoder extends MessageToByteEncoder<NettyPacket<?>> {

  @Override
  protected void encode(ChannelHandlerContext ctx, NettyPacket<?> msg, ByteBuf out) {
    final SurfByteBuf surfByteBuf = new SurfByteBuf(out);
    surfByteBuf.writeInt(msg.id());
    msg.encode(surfByteBuf);
  }
}
