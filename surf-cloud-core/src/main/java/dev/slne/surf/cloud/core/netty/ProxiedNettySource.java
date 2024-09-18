package dev.slne.surf.cloud.core.netty;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.core.netty.common.SourceList;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxiedNettySource extends NettySource {

  private @Nullable CloudServer cloudServer;
  private @Nullable CloudServer lastCloudServer;

  public ProxiedNettySource(NettyBase nettyBase) {
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

  public void broadcastServerInfo(CloudServerInfoAction action, NettySource... sources) {

    if (action == CloudServerInfoAction.UPDATE_SERVER_INFO) {
      // we are calling the update function,
      // so our instance knows when we changed something for us or another source
      //
      // note here that it is a little hacky, but does it job
      ((SourceList<ProxiedSource>) base.container.sourceList()).updateClient(this);
      updateServerInfo(cloudServer());
    }
    // sending packet
    PacketServerInfo info = new PacketServerInfo(new ServerInfoData(cloudServer, action));
    if (sources.length == 0) {
      base.container.broadcast(info);
    } else {
      for (Source source : sources) {
        source.send(info);
      }
    }
  }

  public long serverGuid() {
    return cloudServer != null ? cloudServer.serverGuid() : -1;
  }

  public boolean hasServerGuid() {
    return serverGuid() != -1;
  }

  public Optional<CloudServer> cloudServer() {
    return Optional.ofNullable(cloudServer);
  }

  public Optional<CloudServer> lastCloudServer() {
    return Optional.ofNullable(lastCloudServer);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProxiedNettySource that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return Objects.equals(cloudServer, that.cloudServer)
        && Objects.equals(lastCloudServer, that.lastCloudServer);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(cloudServer);
    result = 31 * result + Objects.hashCode(lastCloudServer);
    return result;
  }
}
