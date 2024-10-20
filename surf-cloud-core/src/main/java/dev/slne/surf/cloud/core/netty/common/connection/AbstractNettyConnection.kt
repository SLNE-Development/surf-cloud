package dev.slne.surf.cloud.core.netty.common.connection;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.api.netty.connection.NettyConnection;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.source.NettySource;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Blocking;

@ParametersAreNonnullByDefault
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
@Flogger
public abstract class AbstractNettyConnection<
    SELF extends AbstractNettyConnection<SELF, Client, B>,
    Client extends ProxiedNettySource<Client>,
    B extends AbstractNettyBase<B, SELF, Client>
    > implements NettyConnection<Client>, AutoCloseable { // Sorry Simon for the number of generics

  private final B base;

  /**
   * Broadcast the packet on servers <br> On clients only to server
   *
   * @param packet the packet to broadcast
   */
  public abstract void broadcast(NettyPacket<?> packet);

  @OverrideOnly
  protected abstract void sendPacket0(AbstractNettySource<Client> source, NettyPacket<?> packet);

  public final void sendPacket(NettySource<?> source, NettyPacket<?> packet) {
    checkNotNull(source, "source");
    checkNotNull(packet, "packet");

    if (!(source instanceof AbstractNettySource<?> sourceImpl)) {
      throw new IllegalArgumentException("source must be an instance of AbstractNettySource");
    }

    base.checkPacket(packet);

    //noinspection unchecked
    sendPacket0((AbstractNettySource<Client>) sourceImpl, packet);
  }

  @Blocking
  public final void tryEstablishConnection() throws Exception {
    try {
      tryEstablishConnection0();
    } catch (Exception e) {
      log.atSevere().withCause(e).log("Connection attempt failed: %s", e.getMessage());
      throw new Exception("Connection attempt failed", e);
    }
  }

  protected abstract void tryEstablishConnection0() throws Exception;
}
