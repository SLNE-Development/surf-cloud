package dev.slne.surf.cloud.standalone.netty.server;

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.data.CloudPersistentData;
import dev.slne.surf.cloud.core.netty.CloudServer;
import dev.slne.surf.cloud.core.netty.CloudServerImpl;
import dev.slne.surf.cloud.core.netty.NettyPacketInfo;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import dev.slne.surf.cloud.core.netty.ServerState;
import dev.slne.surf.cloud.core.netty.client.NettyClientSource;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoBatchPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket.Type;
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudServerRegistrationData;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientJoinNettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientQuitPacket;
import dev.slne.surf.cloud.core.util.Util;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("independent")
public class SurfNettyServerPacketHandler {

  private final SurfNettyServer surfNettyServer;

  public SurfNettyServerPacketHandler(SurfNettyServer surfNettyServer) {
    this.surfNettyServer = surfNettyServer;
  }

  @SurfNettyPacketHandler
  public void onCloudServerRegistration(CloudRegisterServerPacket packet, NettyPacketInfo info) {
    if (!Objects.equals(packet.getType(), Type.FETCH_PRELOAD)) {
      return;
    }

    final CloudServerRegistrationData data = packet.getData();
    final long serverId;

    if (data.serverId() == CloudPersistentData.SERVER_ID_NOT_SET) {
      serverId = Util.getRandom().nextLong(0xffffff);
    } else {
      serverId = data.serverId();
    }

    final CloudServerImpl server = CloudServerImpl.builder()
        .serverGuid(serverId)
        .category(data.category())
        .host(data.host())
        .port(data.port())
        .state(ServerState.RESTARTING)
        .build();

    final NettyClientSource clientSource = info.clientSource();
    clientSource.cloudServer(server);
    clientSource.broadcastServerInfo(CloudServerInfoAction.ADD_SERVER_INFO);

    final CloudRegisterServerPacket preload = CloudRegisterServerPacket.builder()
        .type(Type.PRELOAD)
        .data(CloudServerRegistrationData.builder()
            .serverId(server.serverGuid())
            .category(server.category())
            .port(server.port())
            .host(server.host())
            .build())
        .build();

    clientSource.sendPacket(preload);
  }

  @SurfNettyPacketHandler
  public void onCloudServerInfo(CloudServerInfoPacket packet, NettyPacketInfo info) {
    handleServerInfo(packet.getServer(), packet.getAction());
    broadcastPacket(info.clientSource(), packet);
  }

  @SurfNettyPacketHandler
  public void onCloudServerInfoBatch(CloudServerInfoBatchPacket packet, NettyPacketInfo info) {
    for (final CloudServer server : packet.getServers()) {
      handleServerInfo(server, packet.getAction());
    }
    broadcastPacket(info.clientSource(), packet);
  }

  @SurfNettyPacketHandler
  public void onClientQuit(ClientQuitPacket packet, @NotNull NettyPacketInfo info) {
    final NettyClientSource client = info.clientSource();
    client.cloudServer().ifPresent(server -> {
      server.state(ServerState.OFFLINE);
      client.broadcastServerInfo(CloudServerInfoAction.REMOVE_SERVER_INFO);
      client.cloudServer(null);
    });
  }

  @SurfNettyPacketHandler
  public void onClientJoin(ClientJoinNettyPacket packet, NettyPacketInfo info) {
    final Set<CloudServer> server = surfNettyServer.container().sourceList().clients().stream()
        .filter(ProxiedNettySource::hasServerGuid)
        .map(client -> client.cloudServer().orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    switch (server.size()) {
      case 0 -> {
      }
      case 1 -> info.source().sendPacket(new CloudServerInfoPacket(
          CloudServerInfoAction.ADD_SERVER_INFO,
          server.iterator().next()
      ));
      default -> info.source().sendPacket(new CloudServerInfoBatchPacket(
          CloudServerInfoAction.ADD_SERVER_INFO,
          server
      ));
    }
  }

  private void handleServerInfo(CloudServer server, CloudServerInfoAction action) {
    switch (action) {
      case UPDATE_SERVER_INFO -> {
        final ServerSourceList list = surfNettyServer.container().serverList();
        final NettyClientSource clientSource = list.findByServerGuid(server.serverGuid())
            .orElseThrow();
        clientSource.updateServerInfo(server);
      }
    }
  }

  private void broadcastPacket(NettyClientSource sender, NettyPacket<?> packet) {
    surfNettyServer.container().sourceList().clients().stream()
        .filter(receiver -> !Objects.equals(receiver, sender))
        .forEach(receiver -> receiver.sendPacket(packet));
  }
}
