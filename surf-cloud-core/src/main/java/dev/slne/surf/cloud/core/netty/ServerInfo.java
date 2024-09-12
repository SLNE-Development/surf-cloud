package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.net.InetSocketAddress;
import org.jetbrains.annotations.Range;

public interface ServerInfo {

  Codec<ServerInfo, DecoderException, EncoderException> CODEC = Codec.codec(
  (buf, value) -> {
    buf.writeLong(value.serverGuid());
    buf.writeNullable(value.host());
    buf.writeInt(value.port());
    buf.writeNullable(value.category());
    buf.writeInt(value.currentPlayerCount());
    buf.writeInt(value.maxPlayerCount());
    buf.writeEnum(value.state());
    buf.writeNullable(value.groupId());

    // TODO: 12.09.2024 22:07 - User list
  },
  buf -> {
    final long serverGuid = buf.readLong();
    final String host = buf.readNullable(SurfByteBuf::readString);
    final int port = buf.readInt();
    final String category = buf.readNullable(SurfByteBuf::readString);
    final int currentPlayerCount = buf.readInt();
    final int maxPlayerCount = buf.readInt();
    final ServerState state = buf.readEnum(ServerState.class);
    final String groupId = buf.readNullable(SurfByteBuf::readString);


  }
  );

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

  UserList userList();

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
