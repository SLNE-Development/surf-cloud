package dev.slne.surf.cloud.bukkit.command.connection

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.client.paper.command.args.onlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun timeoutPlayerCommand() = commandTree("timeout") {
    withPermission(CloudPermissionRegistry.TIMEOUT_COMMAND)

    onlineCloudPlayerArgument("player") {
        anyExecutor { sender, args ->
            val player: CloudPlayer by args
            player.disconnectSilent()
            sender.sendText {
                appendPrefix()
                success("Der Spielende ")
                variableValue(player.name)
                success(" wurde erfolgreich still getrennt.")
            }
        }
    }
}