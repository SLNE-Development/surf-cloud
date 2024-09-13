package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.util.ConnectionStateAccessor;
import dev.slne.surf.cloud.api.util.AdvancedAutoCloseable;
import dev.slne.surf.cloud.core.netty.common.SourceList;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@RequiredArgsConstructor
@Getter
public abstract class NettyContainer<Source extends ProxiedNettySource> implements
    AdvancedAutoCloseable, ConnectionStateAccessor {

  private final NettyBase nettyBase;

  public abstract SourceList<Source> sourceList();

  public abstract NettySource source(Channel channel);

  /**
   * Broadcast the packet on servers <br> On clients only to server
   *
   * @param packet the packet to broadcast
   */
  public abstract void broadcast(NettyPacket<?> packet);

  public abstract NettyBase base();

  @OverrideOnly
  protected abstract void sendPacket0(NettySource source, NettyPacket<?> packet);

  public final void sendPacket(NettySource source, NettyPacket<?> packet) {
    nettyBase.checkPacket(packet);
    sendPacket0(source, packet);
  }

}
