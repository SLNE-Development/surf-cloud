package dev.slne.surf.cloud.standalone.netty;

import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl;
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier;
import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer;
import dev.slne.surf.cloud.standalone.netty.server.connection.NettyServerConnectionImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("independent")
public class NettyPacketJoinQuitIndependentHandler implements
    ChannelInitializerModifier {

  private final NettyServerConnectionImpl container;

  public NettyPacketJoinQuitIndependentHandler(SurfNettyServer server) {
    this.container = server.connection();
  }

  @Override
  public void modify(SocketChannel ch) throws Exception {
    ch.pipeline().addBefore("commonJoinQuitHandler", "independentJoinQuitHandler", new Handler());
  }

  private class Handler extends ChannelInboundHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      final NettyClientSourceImpl clientSource = new NettyClientSourceImpl(container.base(), ctx.channel());
      container.clientTracker().addClient(clientSource);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      final NettyClientSource source = container.source(ctx.channel()).orElseThrow();
      container.clientTracker().removeClient(source);
    }
  }
}
