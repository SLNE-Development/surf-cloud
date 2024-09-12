package dev.slne.surf.data.core.test;

import dev.slne.surf.data.api.redis.RedisEventHandler;
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
