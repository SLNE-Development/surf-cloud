package dev.slne.surf.cloud.core.test;

import dev.slne.surf.cloud.api.redis.RedisEventHandler;
import lombok.extern.flogger.Flogger;
import org.springframework.stereotype.Component;

@Component
@Flogger
public class TestPacketListener {

  @RedisEventHandler(TestPacket.CHANNEL)
  public void handle(TestPacket packet) {
    log.atInfo()
        .log("Received packet: %s", packet);
  }
}
