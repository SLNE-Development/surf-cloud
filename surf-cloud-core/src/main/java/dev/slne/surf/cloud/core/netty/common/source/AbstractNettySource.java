package dev.slne.surf.cloud.core.netty.common.source;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.source.NettySource;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@Accessors(fluent = true)
@ParametersAreNonnullByDefault
public abstract class AbstractNettySource<Client extends ProxiedNettySource<Client>> implements NettySource<Client> {

  private final AbstractNettyBase<?, ?, Client> base;

  public AbstractNettySource(AbstractNettyBase<?, ?, Client> base) {
    this.base = base;
  }

  @Override
  public void sendPacket(NettyPacket<?> packet) {
    checkNotNull(packet, "packet");

    base().connection().sendPacket(this, packet);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractNettySource<?> that)) {
      return false;
    }

    return base.equals(that.base);
  }

  @Override
  public int hashCode() {
    return base.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
