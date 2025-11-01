package dev.slne.surf.cloudtest.paper.test.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.paper.command.args.onlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloudtest.core.test.ppdc.PpdcTestExecutor
import dev.slne.surf.cloudtest.paper.bean
import dev.slne.surf.cloudtest.paper.plugin

fun testPpdcCommand() = commandTree("test-ppdc") {
    withPermission(CommandPermission.OP)

    onlineCloudPlayerArgument("player") {
        literalArgument("set") {
            anyExecutor { sender, args ->
                val player: CloudPlayer by args
                plugin.launch {
                    bean<PpdcTestExecutor>().setRandomPpdcTestData(sender, player)
                }
            }
        }
        literalArgument("get") {
            anyExecutor { sender, args ->
                val player: CloudPlayer by args
                plugin.launch {
                    bean<PpdcTestExecutor>().showPpdcTestData(sender, player)
                }
            }
        }
    }
}