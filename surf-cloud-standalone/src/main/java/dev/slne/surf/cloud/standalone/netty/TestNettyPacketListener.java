package dev.slne.surf.cloud.standalone.netty;

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import org.springframework.stereotype.Component;

@Component
public class TestNettyPacketListener {

  @SurfNettyPacketHandler
  public void handleTestPacket(TestNettyPacket packet) {
    System.out.println("Received Test packet: " + packet);
  }

}
