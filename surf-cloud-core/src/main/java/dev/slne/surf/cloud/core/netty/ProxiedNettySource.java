package dev.slne.surf.cloud.core.netty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ProxiedNettySource extends NettySource {

  private CloudServer cloudServer;
  private CloudServer lastCloudServer;

  ProxiedNettySource(NettyBase nettyBase) {
    super(nettyBase);
  }

  public void updateServerInfo(CloudServer other) {
    checkNotNull(other, "other");

    if (this.cloudServer == null) {
      this.cloudServer = other;
      return;
    }

    this.lastCloudServer = this.cloudServer;
    this.cloudServer = other;
  }

  @SuppressWarnings({"LombokSetterMayBeUsed", "RedundantSuppression"})
  public void setCloudServer(CloudServer cloudServer) {
    this.cloudServer = cloudServer;
//    if (this.cloudServer != null) {
//      this.direction = this.cloudServer.getDirection();
//    }
  }

  public void broadcastServerInfo(ServerInfoAction action, Source... sources) {

    if (action == ServerInfoAction.UPDATE_SERVER_INFO) {
      // we are calling the update function,
      // so our instance knows when we changed something for us or another source
      //
      // note here that it is a little hacky, but does it job
      ((SourceList<ProxiedSource>) base.container.sourceList()).updateClient(this);
      updateServerInfo(getCloudServer());
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

  public long getServerGuid() {
    return cloudServer != null ? cloudServer.serverGuid() : -1;
  }

  public boolean hasServerGuid() {
    return getServerGuid() != -1;
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

    return Objects.equals(getCloudServer(), that.getCloudServer())
        && Objects.equals(getLastCloudServer(), that.getLastCloudServer());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(getCloudServer());
    result = 31 * result + Objects.hashCode(getLastCloudServer());
    return result;
  }
}
