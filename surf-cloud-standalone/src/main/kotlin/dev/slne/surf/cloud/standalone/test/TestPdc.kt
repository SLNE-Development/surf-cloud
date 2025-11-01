package dev.slne.surf.cloud.standalone.test

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