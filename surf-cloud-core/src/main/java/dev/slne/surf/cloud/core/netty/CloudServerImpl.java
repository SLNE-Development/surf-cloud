package dev.slne.surf.cloud.core.netty;

import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Range;

@Getter
@Setter
@Accessors(fluent = true)
@Builder
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

  private String groupId;
  private @Range(from = 0, to = 65535) int port;
  private String host;
  private String category;
  private long serverGuid;
  private int currentPlayerCount;
  private int maxPlayerCount;
  private @Default ServerState state = ServerState.OFFLINE;

}
