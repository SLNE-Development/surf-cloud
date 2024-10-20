package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration;

import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;

@Builder
public record CloudServerRegistrationData(
    long serverId,
    @Nullable String category,
    int port,
    @Nullable String host
) {

  public static final Codec<CloudServerRegistrationData, DecoderException, EncoderException> CODEC = Codec.codec(
      (buf, data) -> {
        buf.writeLong(data.serverId());
        buf.writeNullable(data.category());
        buf.writeInt(data.port());
        buf.writeNullable(data.host());
      },
      buf -> new CloudServerRegistrationData(
          buf.readLong(),
          buf.readNullableString(),
          buf.readInt(),
          buf.readNullableString()
      )
  );
}
