package dev.slne.surf.cloud.standalone.netty;

import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestNettyPacketSender implements CommandLineRunner {


  private final SurfNettyServer surfNettyServer;

  public TestNettyPacketSender(SurfNettyServer surfNettyServer) {
    this.surfNettyServer = surfNettyServer;
  }

  @Override
  public void run(String... args) throws Exception {
    System.err.println("###################");
    System.err.println("Sending test packet");
    System.err.println("###################");

    TestNettyPacket packet = TestNettyPacket.builder()
        .test("Test")
        .testInt(1)
        .testBoolean(true)
        .testUUID(UUID.randomUUID())
        .build();

    System.err.println("Sending packet: " + packet);
    surfNettyServer.connection().broadcast(packet);
  }
}
