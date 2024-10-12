package dev.slne.surf.cloud.standalone.netty.server.source.tracker;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.api.netty.source.tracker.ServerNettyClientTracker;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.common.source.tracker.NettyClientTrackerImpl;
import io.netty.channel.Channel;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public final class ServerNettyClientTrackerImpl extends
    NettyClientTrackerImpl<NettyClientSource> implements ServerNettyClientTracker {

  public ServerNettyClientTrackerImpl(AbstractNettyBase<?, ?, NettyClientSource> nettyBase) {
    super(nettyBase);
  }

  @Override
  public @NotNull Optional<NettyClientSource> client(Channel channel) {
    checkNotNull(channel, "channel");

    return clients().stream()
        .filter(source -> source.channel() == channel)
        .findFirst();
  }
}
