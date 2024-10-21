package dev.slne.surf.cloud.core.netty.client.source;

import dev.slne.surf.cloud.api.netty.source.NettyClientSource;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.common.source.AbstractProxiedNettySource;
import io.netty.channel.Channel;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class NettyClientSourceImpl extends AbstractProxiedNettySource<NettyClientSource> implements NettyClientSource {

  private final Channel channel;

  public NettyClientSourceImpl(AbstractNettyBase<?, ?, NettyClientSource> base, Channel channel) {
    super(base);
    this.channel = channel;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NettyClientSourceImpl that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return Objects.equals(channel, that.channel);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(channel);
    return result;
  }
}
