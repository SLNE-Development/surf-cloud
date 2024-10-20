package dev.slne.surf.cloud.core.netty.common.source;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.api.server.CloudServer;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoPacket;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractProxiedNettySource<Client extends ProxiedNettySource<Client>> extends
    AbstractNettySource<Client> implements ProxiedNettySource<Client> {

  private @Nullable CloudServer cloudServer;
  private @Nullable CloudServer lastCloudServer;

  public AbstractProxiedNettySource(AbstractNettyBase<?, ?, Client> nettyBase) {
    super(nettyBase);
  }

  public void updateServerInfo(@NotNull CloudServer other) {
    checkNotNull(other, "other");

    if (this.cloudServer == null) {
      this.cloudServer = other;
      return;
    }

    this.lastCloudServer = this.cloudServer;
    this.cloudServer = other;
  }

  @SuppressWarnings({"LombokSetterMayBeUsed", "RedundantSuppression"})
  public void cloudServer(@Nullable CloudServer cloudServer) {
    this.cloudServer = cloudServer;
//    if (this.cloudServer != null) {
//      this.direction = this.cloudServer.getDirection();
//    }
  }

  public void broadcastServerInfo(CloudServerInfoAction action,
      @NotNull AbstractNettySource<?> @NotNull ... sources) {
    if (action == CloudServerInfoAction.UPDATE_SERVER_INFO) {
      // we are calling the update function,
      // so our instance knows when we changed something for us or another source
      //
      // note here that it is a little hacky, but does it job

//      ((NettyClientTrackerImpl<ProxiedSource>) base.container.sourceTracker()).updateClient(this); TODO not needed or replace with spring events
      assert cloudServer != null : "We should have a cloud server here";
      updateServerInfo(cloudServer);
    }
    final CloudServerInfoPacket packet = new CloudServerInfoPacket(action, cloudServer);

    if (sources.length == 0) {
      base().connection().broadcast(packet);
    } else {
      for (final @NotNull AbstractNettySource<?> source : sources) {
        source.sendPacket(packet);
      }
    }
  }

  @Override
  public long serverGuid() {
    return cloudServer != null ? cloudServer.serverGuid() : -1;
  }

  @Override
  public boolean hasServerGuid() {
    return serverGuid() != -1;
  }

  @Override
  public Optional<CloudServer> cloudServer() {
    return Optional.ofNullable(cloudServer);
  }

  @Override
  public Optional<CloudServer> lastCloudServer() {
    return Optional.ofNullable(lastCloudServer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractProxiedNettySource<?> that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return Objects.equals(cloudServer, that.cloudServer) && Objects.equals(
        lastCloudServer, that.lastCloudServer);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(cloudServer);
    result = 31 * result + Objects.hashCode(lastCloudServer);
    return result;
  }
}
