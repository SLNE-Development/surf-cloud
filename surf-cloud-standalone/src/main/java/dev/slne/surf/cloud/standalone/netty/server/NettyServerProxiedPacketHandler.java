package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.core.netty.common.ChannelInitializerModifier;
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("independent")
public class NettyServerProxiedPacketHandler extends ChannelDuplexHandler implements
    ChannelInitializerModifier {

  @Override
  public void modify(SocketChannel ch) throws Exception {
    ch.pipeline().addBefore("packetHandler", "proxiedPacketHandler", this);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof final ProxiedNettyPacket proxiedNettyPacket) {
      proxiedNettyPacket.target().sendPacket(proxiedNettyPacket);
    } else {
      super.channelRead(ctx, msg);
    }
  }
}
