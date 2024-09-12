package dev.slne.surf.cloud.core.netty.common;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.springframework.stereotype.Component;

@Component
public class SurfNettyChannelInitializer extends ChannelInitializer<SocketChannel> {

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline()
        .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
        .addLast("frameEncoder", new LengthFieldPrepender(4, false));
  }
}
