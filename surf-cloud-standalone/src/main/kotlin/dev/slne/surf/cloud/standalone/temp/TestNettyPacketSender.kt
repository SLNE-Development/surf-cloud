package dev.slne.surf.cloud.standalone.temp

import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import java.util.*

@Component
class TestNettyPacketSender(private val nettyServerImpl: NettyServerImpl) : SmartLifecycle {

    private var running = false

    override fun start() {
        running = true
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
        nettyServerImpl.connection.broadcast(packet)
    }

    override fun stop() {
        System.err.println("###################")
        System.err.println("Stopping test packet sender")
        System.err.println("###################")
        running = false

    }

    override fun isRunning(): Boolean {
        return running
    }
}
