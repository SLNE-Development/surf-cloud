package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PlayerConnectionLogger {

    private val log = logger()

    @EventListener
    fun onCloudPlayerConnectToNetwork(event: CloudPlayerConnectToNetworkEvent) {
        if (!standaloneConfig.logging.logPlayerConnections) return
        val player = event.player as StandaloneCloudPlayerImpl

        log.atInfo()
            .log("Player ${player.name} (${player.uuid}) connected to the network")
    }
}