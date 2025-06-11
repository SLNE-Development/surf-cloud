package dev.slne.surf.cloudtest.standalone.test.punish

import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import org.springframework.stereotype.Component

@Component
class TestCloudEventListener {

    @CloudEventHandler
    fun onEvent(event: CloudEvent) {
        println("Received event: ${event::class.simpleName} with data: $event")
    }

    @CloudEventHandler
    fun onBan(event: CloudPlayerPunishEvent<PunishmentBan>) {
        println("Received ban event: ${event::class.simpleName} with data: $event")
    }
}