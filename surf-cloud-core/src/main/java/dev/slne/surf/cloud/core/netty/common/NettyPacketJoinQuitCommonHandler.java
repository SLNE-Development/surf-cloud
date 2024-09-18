package dev.slne.surf.cloud.core.netty.common;

import dev.slne.surf.cloud.core.netty.NettyContainer;
import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientJoinNettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientQuitPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyPacketJoinQuitCommonHandler extends ChannelInboundHandlerAdapter {

  private final NettyContainer<?, ?, ?> container;

  public NettyPacketJoinQuitCommonHandler(NettyContainer<?, ?, ?> container) {
    this.container = container;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    final NettySource source = container.source(ctx.channel()).orElseThrow();
    container.base().onPacketReceived(new ClientJoinNettyPacket(), source, null);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    final NettySource source = container.source(ctx.channel()).orElseThrow();
    container.base().onPacketReceived(new ClientQuitPacket(), source, null);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    super.channelUnregistered(ctx);
  }

}
