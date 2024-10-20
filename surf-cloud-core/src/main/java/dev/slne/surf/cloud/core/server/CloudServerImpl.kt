package dev.slne.surf.cloud.core.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import dev.slne.surf.cloud.api.server.CloudServer;
import dev.slne.surf.cloud.api.server.state.ServerState;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@Getter
@Accessors(fluent = true)
@Builder
@ParametersAreNonnullByDefault
public final class CloudServerImpl implements CloudServer {

  public static final Codec<CloudServer, DecoderException, EncoderException> CODEC = Codec.codec(
      (buf, value) -> {
        buf.writeLong(value.serverGuid());
        buf.writeNullable(value.host()); // TODO: 15.09.2024 10:29 - why nullable?
        buf.writeInt(value.port());
        buf.writeNullable(value.category());
        buf.writeInt(value.currentPlayerCount());
        buf.writeInt(value.maxPlayerCount());
        buf.writeEnum(value.state());
        buf.writeNullable(value.groupId());
      },
      buf -> CloudServerImpl.builder()
          .serverGuid(buf.readLong())
          .host(buf.readNullable(SurfByteBuf::readString))
          .port(buf.readInt())
          .category(buf.readNullable(SurfByteBuf::readString))
          .currentPlayerCount(buf.readInt())
          .maxPlayerCount(buf.readInt())
          .state(buf.readEnum(ServerState.class))
          .groupId(buf.readNullable(SurfByteBuf::readString))
          .build()
  );

  private @Nullable String groupId;
  private @Range(from = 0, to = 65535) int port;
  private @Nullable String host;
  private @Nullable String category;
  private long serverGuid;
  private int currentPlayerCount;
  private int maxPlayerCount;
  private @Default ServerState state = ServerState.OFFLINE;

  @Override
  public void groupId(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public void port(int port) {
    checkArgument(port >= 0 && port <= 65535, "Port must be in range 0-65535");

    this.port = port;
  }

  @Override
  public void host(String host) {
    this.host = host;
  }

  @Override
  public void category(String category) {
    this.category = category;
  }

  @Override
  public void serverGuid(long serverGuid) {
    this.serverGuid = serverGuid;
  }

  @Override
  public void currentPlayerCount(int currentPlayerCount) {
    this.currentPlayerCount = currentPlayerCount;
  }

  @Override
  public void maxPlayerCount(int maxPlayerCount) {
    this.maxPlayerCount = maxPlayerCount;
  }

  @Override
  public void state(ServerState state) {
    checkNotNull(state, "state");

    this.state = state;
  }
}
