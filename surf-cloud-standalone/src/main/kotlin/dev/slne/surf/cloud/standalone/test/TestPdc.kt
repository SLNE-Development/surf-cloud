package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.messages.Colors
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class TestPdc {
    private val logger = ComponentLogger.logger()

    @EventListener
    fun onCloudPlayerConnectToNetwork(event: CloudPlayerConnectToNetworkEvent) {
        logger.info(
            Component.text(
                "Player ${event.player.uuid} connected to the network",
                Colors.SUCCESS
            )
        )

        event.player.sendMessage(
            Component.text(
                "Welcome to the network!",
                Colors.SUCCESS
            )
        )

        (event.player as StandaloneCloudPlayerImpl).writeTestToPdc()
    }
}