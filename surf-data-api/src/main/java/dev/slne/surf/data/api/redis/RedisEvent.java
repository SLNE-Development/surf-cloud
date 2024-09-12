package dev.slne.surf.data.api.redis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.slne.surf.data.api.SurfDataInstance;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class RedisEvent {

  @Getter(onMethod_ = @JsonIgnore)
  private String eventName;

  @Getter(onMethod_ = @JsonIgnore)
  private String[] channels;
  private UUID packetId;

  public RedisEvent(String firstChannel, String... channels) {
    this.channels = ArrayUtils.add(channels, firstChannel);
    this.packetId = UUID.randomUUID();
  }

  public final void send() {
    SurfDataInstance.get().callRedisEvent(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
