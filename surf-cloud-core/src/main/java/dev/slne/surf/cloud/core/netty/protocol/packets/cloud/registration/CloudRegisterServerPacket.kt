package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.meta.SurfNettyPacket.DefaultIds;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@SurfNettyPacket(id = DefaultIds.CLOUD_REGISTER_SERVER_PACKET)
@Builder
public class CloudRegisterServerPacket extends NettyPacket<CloudRegisterServerPacket> {

  private Type type;
  private CloudServerRegistrationData data;

  @Override
  public void encode(SurfByteBuf buffer) {
    buffer.writeEnum(type);
    buffer.writeWithCodec(CloudServerRegistrationData.CODEC, data);
  }

  @Override
  public CloudRegisterServerPacket decode(SurfByteBuf buffer) {
    type = buffer.readEnum(Type.class);
    data = buffer.readWithCodec(CloudServerRegistrationData.CODEC);
    return this;
  }


  public enum Type {
    /**
     * Requests preload
     */
    FETCH_PRELOAD,

    /**
     * Contains all information
     */
    PRELOAD
  }
}
