package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.util.ConnectionStateAccessor;
import dev.slne.surf.cloud.api.util.AdvancedAutoCloseable;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import io.netty.channel.Channel;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public abstract class NettyContainer<
    SELF extends NettyContainer<SELF, Source, B>,
    Source extends ProxiedNettySource,
    B extends NettyBase<SELF>>
    implements AdvancedAutoCloseable, ConnectionStateAccessor { // Sorry Simon for the number of generics

  private final B base;

  public abstract SourceList<Source> sourceList();

  public abstract Optional<? extends NettySource> source(Channel channel);

  /**
   * Broadcast the packet on servers <br> On clients only to server
   *
   * @param packet the packet to broadcast
   */
  public abstract void broadcast(NettyPacket<?> packet);

  @OverrideOnly
  protected abstract void sendPacket0(NettySource source, NettyPacket<?> packet);

  public final void sendPacket(NettySource source, NettyPacket<?> packet) {
    base.checkPacket(packet);
    sendPacket0(source, packet);
  }

}
