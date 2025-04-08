package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.springframework.context.event.EventListener
import kotlin.time.Duration.Companion.seconds
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class TestPdc {
//    private val logger = ComponentLogger.logger()
//
//    @EventListener
//    fun onCloudPlayerConnectToNetwork(event: CloudPlayerConnectToNetworkEvent) {
//        val player = event.player as StandaloneCloudPlayerImpl
//        logger.info(
////            Component.text(
////                "Player ${player.uuid} connected to the network",
////                Colors.SUCCESS
////            )
//            buildText {
//                info("Player ")
//                variableValue("${player.name} (${player.uuid})")
//                info(" connected to the network")
//            }
//        )
//
//        player.sendMessage(
//            Component.text(
//                "Welcome to the network!",
//                Colors.SUCCESS
//            )
//        )
//
//
////        GlobalScope.launch {
////            while (player.connected) {
////                delay(1.seconds)
////                logger.info("pdc: ${player.getPersistentData()}")
////            }
////        }
//    }
}