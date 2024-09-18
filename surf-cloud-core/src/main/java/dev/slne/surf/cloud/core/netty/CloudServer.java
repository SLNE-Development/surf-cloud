package dev.slne.surf.cloud.core.netty;

import java.net.InetSocketAddress;
import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;

@NonExtendable
@ParametersAreNonnullByDefault
public interface CloudServer {

  String groupId();

  void groupId(String groupId);

  @Range(from = 0, to = 65535)
  int port();

  void port(@Range(from = 0, to = 65535) int port);

  String host();

  void host(String host);

  default String address() {
    return host() + ":" + port();
  }

  default InetSocketAddress inetSocketAddress() {
    return new InetSocketAddress(host(), port());
  }

  default void inetSocketAddress(InetSocketAddress inetSocketAddress) {
    host(inetSocketAddress.getHostString());
    port(inetSocketAddress.getPort());
  }

  default String getRenderedName() {
    return category() + "-" + Long.toHexString(serverGuid());
  }

//  UserList userList();

  String category();

  void category(String category);

  long serverGuid();

  void serverGuid(long serverGuid);

  int currentPlayerCount();

  void currentPlayerCount(int currentPlayerCount);

  int maxPlayerCount();

  void maxPlayerCount(int maxPlayerCount);

  ServerState state();

  void state(ServerState state);
}
