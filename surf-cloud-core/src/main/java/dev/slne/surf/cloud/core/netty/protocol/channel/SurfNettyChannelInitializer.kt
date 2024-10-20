package dev.slne.surf.cloud.core.netty.protocol.channel;

import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry;
import dev.slne.surf.cloud.core.netty.protocol.codec.NettyPacketDecoder;
import dev.slne.surf.cloud.core.netty.protocol.codec.NettyPacketEncoder;
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.NettyPacketHandler;
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.NettyPacketJoinQuitCommonHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Flogger
@Component
public class SurfNettyChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final NettyPacketRegistry packetRegistry;
  private final ObjectProvider<ChannelInitializerModifier> modifiers;
  private final ConfigurableApplicationContext context;

  public SurfNettyChannelInitializer(
      NettyPacketRegistry packetRegistry,
      ObjectProvider<ChannelInitializerModifier> modifiers,
      ConfigurableApplicationContext context
  ) {
    this.packetRegistry = packetRegistry;
    this.modifiers = modifiers;
    this.context = context;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    final AbstractNettyBase<?, ?, ?> base = context.getBean(AbstractNettyBase.class);
    ch.pipeline()
        .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
        .addLast("frameEncoder", new LengthFieldPrepender(4, false))
        .addLast("decoder", new NettyPacketDecoder(packetRegistry))
        .addLast("encoder", new NettyPacketEncoder())
        .addLast("commonJoinQuitHandler", new NettyPacketJoinQuitCommonHandler(base.connection()))
        .addLast("packetHandler", new NettyPacketHandler(base))
        .addLast("logger", new LoggingHandler(LogLevel.INFO))
        .addLast("exceptionHandler", new ChannelDuplexHandler() {
          @Override
          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.atSevere().withCause(cause).log("Exception caught in channel %s", ctx.channel());
          }
        });

    for (final ChannelInitializerModifier modifier : modifiers.orderedStream().toList()) {
      modifier.modify(ch);
    }
  }
}
