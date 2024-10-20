package dev.slne.surf.cloud.core.netty.protocol.packet.handler;

import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientJoinNettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientQuitPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyPacketJoinQuitCommonHandler extends ChannelInboundHandlerAdapter {

  private final AbstractNettyConnection<?, ?, ?> container;

  public NettyPacketJoinQuitCommonHandler(AbstractNettyConnection<?, ?, ?> container) {
    this.container = container;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    container.source(ctx.channel()).ifPresent(
        source -> container.base().onPacketReceived(new ClientJoinNettyPacket(), source, null));
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    container.source(ctx.channel()).ifPresent(
        source -> container.base().onPacketReceived(new ClientQuitPacket(), source, null));
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
  }

}
