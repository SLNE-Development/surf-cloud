package dev.slne.surf.cloud.core.netty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import lombok.Getter;

@Getter
public class ProxiedNettySource extends NettySource {

  private ServerInfo serverInfo;
  private ServerInfo lastServerInfo;

  ProxiedNettySource(NettyBase nettyBase) {
    super(nettyBase);
  }

  public void updateServerInfo(ServerInfo other) {
    checkNotNull(other, "other");

    if (this.serverInfo == null) {
      this.serverInfo = other;
      return;
    }

    this.lastServerInfo = this.serverInfo;
    this.serverInfo = other;
  }

  @SuppressWarnings({"LombokSetterMayBeUsed", "RedundantSuppression"})
  public void setServerInfo(ServerInfo serverInfo) {
    this.serverInfo = serverInfo;
//    if (this.serverInfo != null) {
//      this.direction = this.serverInfo.getDirection();
//    }
  }

  public void broadcastServerInfo(ServerInfoAction action, Source... sources) {

    if (action == ServerInfoAction.UPDATE_SERVER_INFO) {
      // we are calling the update function,
      // so our instance knows when we changed something for us or another source
      //
      // note here that it is a little hacky, but does it job
      ((SourceList<ProxiedSource>) base.container.sourceList()).updateClient(this);
      updateServerInfo(getServerInfo());
    }
    // sending packet
    PacketServerInfo info = new PacketServerInfo(new ServerInfoData(serverInfo, action));
    if (sources.length == 0) {
      base.container.broadcast(info);
    } else {
      for (Source source : sources) {
        source.send(info);
      }
    }
  }

  public long getServerGuid() {
    return serverInfo != null ? serverInfo.serverGuid() : -1;
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

    return Objects.equals(getServerInfo(), that.getServerInfo())
        && Objects.equals(getLastServerInfo(), that.getLastServerInfo());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(getServerInfo());
    result = 31 * result + Objects.hashCode(getLastServerInfo());
    return result;
  }
}
