package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.standalone.redis.RedisEventHandler
import org.springframework.stereotype.Component


@Component
class TestPacketListener {
    private val log = logger()

    @RedisEventHandler(TestPacket.CHANNEL)
    fun handle(packet: TestPacket?) {
        log.atInfo()
            .log("Received packet: %s", packet)
    }
}
