package dev.slne.surf.cloud.bukkit.command.punish

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import org.bukkit.entity.Player

private val log = logger()

fun anticheatBanCommand() = commandTree("acban") {
    withPermission(CloudPermissionRegistry.ANTICHEAT_BAN_COMMAND)
    entitySelectorArgumentOnePlayer("player") {
        anyExecutor { sender, args ->
            val player: Player by args
            val offlinePlayer =
                player.toOfflineCloudPlayer() ?: throw CommandAPI.failWithString("Player not found")

            plugin.launch {
                try {
                    offlinePlayer.punishmentManager.anticheatBan()

                    sender.sendText {
                        appendPrefix()
                        success("Der Spieler ")
                        variableValue(player.name)
                        success(" wurde erfolgreich gebannt.")
                    }
                } catch (e: Throwable) {
                    log.atSevere()
                        .withCause(e)
                        .log("Failed to ban player ${player.name}")
                    sender.sendText {
                        appendPrefix()
                        error("Der Spieler ")
                        variableValue(player.name)
                        error(" konnte nicht gebannt werden.")
                    }
                }
            }
        }
    }
}