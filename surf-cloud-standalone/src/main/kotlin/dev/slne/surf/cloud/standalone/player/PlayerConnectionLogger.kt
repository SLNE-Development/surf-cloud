package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
class PlayerConnectionLogger(private val configHolder: StandaloneConfigHolder) {

    private val log = logger()

    @CloudEventHandler
    fun onCloudPlayerConnectToNetwork(event: CloudPlayerConnectToNetworkEvent) {
        if (!configHolder.config.serverLogging.logPlayerConnections) return
        val player = event.player as StandaloneCloudPlayerImpl

        log.atInfo()
            .log("Player ${player.name} (${player.uuid}) connected to the network")
    }

    @CloudEventHandler
    fun onCloudPlayerDisconnectFromNetwork(event: CloudPlayerDisconnectFromNetworkEvent) {
        if (!configHolder.config.serverLogging.logPlayerConnections) return
        val player = event.player as StandaloneCloudPlayerImpl

        log.atInfo()
            .log("Player ${player.name} (${player.uuid}) disconnected from the network")
    }
}