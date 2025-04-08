package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.common.TestPacket
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.stereotype.Component


@Component
class TestPacketListener {
    private val log = logger()

    @SurfNettyPacketHandler
    fun onTestPacket(packet: TestPacket) {
        log.atInfo()
            .log("Received TestPacket: $packet")
    }
}
