package dev.slne.surf.cloud.standalone.netty;

import dev.slne.surf.cloud.api.meta.SurfNettyPacket;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SurfNettyPacket(id = 0xff)
public class TestNettyPacket extends NettyPacket<TestNettyPacket> {

  private String test;
  private int testInt;
  private boolean testBoolean;
  private @Nullable UUID testUUID;

  @Override
  public void encode(SurfByteBuf buffer) {
    buffer.writeString(test);
    buffer.writeInt(testInt);
    buffer.writeBoolean(testBoolean);
    buffer.writeNullable(testUUID);
  }

  @Override
  public TestNettyPacket decode(SurfByteBuf buffer) {
    test = buffer.readString();
    testInt = buffer.readInt();
    testBoolean = buffer.readBoolean();
    testUUID = buffer.readNullableUuid();
    return this;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TestNettyPacket that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return getTestInt() == that.getTestInt() && isTestBoolean() == that.isTestBoolean()
        && Objects.equals(getTest(), that.getTest()) && Objects.equals(
        getTestUUID(), that.getTestUUID());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(getTest());
    result = 31 * result + getTestInt();
    result = 31 * result + Boolean.hashCode(isTestBoolean());
    result = 31 * result + Objects.hashCode(getTestUUID());
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
