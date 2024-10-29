package dev.slne.surf.cloud.standalone.temp

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler
import org.springframework.stereotype.Component

@Component
class TestNettyPacketListener {
    @SurfNettyPacketHandler
    suspend fun handleTestPacket(packet: TestNettyPacket) {
        println("Received Test packet: $packet")
    }
}
