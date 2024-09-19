package dev.slne.surf.cloud.core.netty.common;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.flogger.Flogger;

@Flogger
public final class NettyPacketHandler extends ChannelDuplexHandler {

  private final NettyBase<?> base;

  public NettyPacketHandler(NettyBase<?> base) {
    this.base = base;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!(msg instanceof final NettyPacket<?> packet)) {
      super.channelRead(ctx, msg);
      return;
    }

    final ProxiedNettySource proxiedSource;
    final NettyPacket<?> finalPacket;
    if (packet instanceof final ProxiedNettyPacket proxiedNettyPacket) {
      finalPacket = proxiedNettyPacket.packet();
      proxiedSource = proxiedNettyPacket.source();
    } else {
      finalPacket = packet;
      proxiedSource = null;
    }

    final NettySource source = base.container().source(ctx.channel()).orElseThrow();
    base.onPacketReceived(finalPacket, source, proxiedSource);

    // if !cancelled // TODO: Implement this?
    super.channelRead(ctx, msg);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception { // TODO: 18.09.2024 18:05 - currently not needed. Ever needed?
//    if (!(msg instanceof final NettyPacket<?> packet)) {
//      super.write(ctx, msg, promise);
//      return;
//    }
//
//    final ProxiedNettySource proxiedSource;
//    final NettyPacket<?> finalPacket;
//    if (packet instanceof final ProxiedNettyPacket proxiedNettyPacket) {
//      finalPacket = proxiedNettyPacket.getPacket();
//      proxiedSource = proxiedNettyPacket.getSource();
//    } else {
//      finalPacket = packet;
//      proxiedSource = null;
//    }
//
//    final NettySource source = base.container().source(ctx.channel()).orElseThrow();
//    base.onPacketSent(finalPacket, source, proxiedSource);
//
//    // if !cancelled // TODO: Implement this?
    super.write(ctx, msg, promise);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    log.atInfo()
        .log("Joined. (%s)", ctx.channel().remoteAddress());
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) {
    log.atInfo()
        .log("Quitted. (%s)", ctx.channel().remoteAddress());
  }
}
