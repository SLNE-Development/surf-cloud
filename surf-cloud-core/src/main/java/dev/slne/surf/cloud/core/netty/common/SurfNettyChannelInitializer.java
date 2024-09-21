package dev.slne.surf.cloud.core.netty.common;

import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.common.registry.packet.SurfPacketRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class SurfNettyChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final SurfPacketRegistry surfPacketRegistry;
  private final ObjectProvider<ChannelInitializerModifier> modifiers;
  private final ObjectProvider<NettyBase<?>> base;

  public SurfNettyChannelInitializer(
      SurfPacketRegistry surfPacketRegistry,
      ObjectProvider<ChannelInitializerModifier> modifiers,
      ObjectProvider<NettyBase<?>> base
  ) {
    this.surfPacketRegistry = surfPacketRegistry;
    this.modifiers = modifiers;
    this.base = base;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline()
        .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
        .addLast("frameEncoder", new LengthFieldPrepender(4, false))
        .addLast("decoder", new NettyPacketDecoder(surfPacketRegistry))
        .addLast("encoder", new NettyPacketEncoder())
        .addLast("commonJoinQuitHandler", new NettyPacketJoinQuitCommonHandler(base.getObject().container()))
        .addLast("packetHandler", new NettyPacketHandler(base.getObject()));

    for (final ChannelInitializerModifier modifier : modifiers.orderedStream().toList()) {
      modifier.modify(ch);
    }
  }

  @Component
  @Profile("client")
  static class ClientChannelInitializerModifier implements ChannelInitializerModifier {

    @Override
    public void modify(SocketChannel ch) throws Exception {
//       ch.pipeline().addLast("reconnectHandler", new ReconnectHandler(base));
    }
  }
}
