package dev.slne.surf.cloud.bukkit.command.cloud

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.core.client.config.ClientConfigHolder
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.command.CommandSender

fun cloudCommand() = commandTree("cloud") {
    withPermission(CloudPermissionRegistry.CLOUD_COMMAND)

    literalArgument("reload") {
        withPermission(CloudPermissionRegistry.CLOUD_COMMAND_RELOAD)

        anyExecutor { sender, _ ->
            reload(sender)
        }
    }
}

private fun reload(sender: CommandSender) {
    bean<ClientConfigHolder>().reloadFromFile()
    sender.sendText {
        appendPrefix()
        success("Cloud configuration reloaded successfully.")
    }
}