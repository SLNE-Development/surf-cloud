package dev.slne.surf.cloud.core.netty.common;

import io.netty.channel.socket.SocketChannel;

public interface ChannelInitializerModifier {

  void modify(SocketChannel ch) throws Exception;
}
