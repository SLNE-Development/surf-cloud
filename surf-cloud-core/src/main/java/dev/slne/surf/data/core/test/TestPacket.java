package dev.slne.surf.data.core.test;

import dev.slne.surf.data.api.redis.RedisEvent;
import lombok.Getter;

public class TestPacket extends RedisEvent {

  public static final String CHANNEL = "surf:test:channel";

  @Getter
  private TestEntity entity;

  public TestPacket() {
  }

  public TestPacket(TestEntity entity) {
    super(CHANNEL);
    this.entity = entity;
  }
}
