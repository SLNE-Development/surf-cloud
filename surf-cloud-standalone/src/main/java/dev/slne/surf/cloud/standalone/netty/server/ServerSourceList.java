package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.client.NettyClientSource;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import io.netty.channel.Channel;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ServerSourceList extends SourceList<NettyClientSource> {

  public ServerSourceList(NettyBase nettyBase) {
    super(nettyBase);
  }

  public @NotNull Optional<NettyClientSource> client(Channel channel) {
    return clients().stream()
        .filter(source -> source.channel() == channel)
        .findFirst();
  }
}
