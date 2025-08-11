package dev.slne.surf.cloud.bukkit.command.playtime

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.cloud.api.client.paper.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.command.CommandSender

fun playtimeCommand() = commandTree("playtime") {
    withPermission(CloudPermissionRegistry.PLAYTIME_COMMAND)

    playerExecutor { sender, args ->
        sendPlaytime(sender, sender.toCloudPlayer() ?: throw AssertionError("Player is null"))
    }
    offlineCloudPlayerArgument("player") {
        withPermission(CloudPermissionRegistry.PLAYTIME_COMMAND_OTHER)
        anyExecutor { sender, args ->
            val player: OfflineCloudPlayer? by args
            player?.let { sendPlaytime(sender, it) }
        }
    }
}

private fun sendPlaytime(sender: CommandSender, player: OfflineCloudPlayer) = plugin.launch {
    val playtime = player.playtime()
    val complete = playtime.sumPlaytimes()
    val playtimeMap = playtime.playtimePerCategoryPerServer()

    sender.sendText {
        appendPrefix()
        info("Spielzeit für ")
        variableValue("${player.name()} (${player.uuid})")
        appendNewPrefixedLine()
        appendNewPrefixedLine {
            variableKey("Gesamt")
            spacer(": ")
            variableValue(complete.toString())
        }
        appendNewPrefixedLine()
        for ((group, groupServer) in playtimeMap) {
            appendNewPrefixedLine {
                spacer("- ")
                variableKey(group)
                spacer(": ")
                variableValue(playtime.sumByCategory(group).toString())

                for ((serverName, playtime) in groupServer) {
                    appendNewPrefixedLine {
                        text("    ")
                        variableKey(serverName)
                        spacer(": ")
                        variableValue(playtime.toString())
                    }
                }
                appendNewPrefixedLine()
            }
        }
    }
}