package dev.slne.surf.cloud.core.netty.client.connection;

import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.flogger.Flogger;

@Flogger
//@Component
public class TestInitlizier implements ChannelInitializerModifier {

  @Override
  public void modify(SocketChannel ch) throws Exception {
    log.atInfo().log("Modifying channel");
    ch.pipeline().addLast(new ReadTimeoutHandler(5));
  }
}
