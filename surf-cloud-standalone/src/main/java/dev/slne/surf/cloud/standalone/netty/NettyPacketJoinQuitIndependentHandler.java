package dev.slne.surf.cloud.standalone.netty;

import dev.slne.surf.cloud.core.netty.NettySource;
import dev.slne.surf.cloud.core.netty.client.NettyClientSource;
import dev.slne.surf.cloud.core.netty.common.ChannelInitializerModifier;
import dev.slne.surf.cloud.standalone.netty.server.NettyServerContainer;
import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("independent")
public class NettyPacketJoinQuitIndependentHandler extends ChannelInboundHandlerAdapter implements
    ChannelInitializerModifier {

  private final NettyServerContainer container;

  public NettyPacketJoinQuitIndependentHandler(SurfNettyServer server) {
    this.container = server.container();
  }

  @Override
  public void modify(SocketChannel ch) throws Exception {
    ch.pipeline().addBefore("commonJoinQuitHandler", "independentJoinQuitHandler", this);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    final NettyClientSource clientSource = new NettyClientSource(container.base(), ctx.channel());
    container.sourceList().addClient(clientSource);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    final NettySource source = container.source(ctx.channel()).orElseThrow();
    container.sourceList().removeClient((NettyClientSource) source);
  }
}
