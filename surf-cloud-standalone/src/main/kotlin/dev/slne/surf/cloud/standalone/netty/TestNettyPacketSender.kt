package dev.slne.surf.cloud.standalone.netty

import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*

@Component
class TestNettyPacketSender(private val surfNettyServer: SurfNettyServer) : CommandLineRunner {
    override fun run(vararg args: String) {
        System.err.println("###################")
        System.err.println("Sending test packet")
        System.err.println("###################")

        val packet = TestNettyPacket(
            test = "Test",
            testInt = 1,
            testBoolean = true,
            testUUID = UUID.randomUUID()
        )

        System.err.println("Sending packet: $packet")
        surfNettyServer.connection.broadcast(packet)
    }
}
