package dev.slne.surf.cloud.core.netty.protocol.channel;

import io.netty.channel.socket.SocketChannel;

public interface ChannelInitializerModifier {

  void modify(SocketChannel ch) throws Exception;
}
