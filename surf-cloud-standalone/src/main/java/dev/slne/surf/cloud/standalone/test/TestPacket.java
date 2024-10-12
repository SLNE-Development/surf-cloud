package dev.slne.surf.cloud.standalone.test;


import dev.slne.surf.cloud.standalone.redis.RedisEvent;
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
