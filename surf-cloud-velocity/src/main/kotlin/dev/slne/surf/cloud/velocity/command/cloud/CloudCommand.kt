package dev.slne.surf.cloud.velocity.command.cloud

import com.velocitypowered.api.command.CommandSource
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.core.client.config.ClientConfigHolder
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.velocity.permission.VelocityPermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun cloudCommand() = commandTree("cloud") {
    withPermission(VelocityPermissionRegistry.CLOUD_COMMAND)

    literalArgument("reload") {
        withPermission(VelocityPermissionRegistry.CLOUD_COMMAND_RELOAD)

        anyExecutor { sender, _ ->
            reload(sender)
        }
    }
}

private fun reload(sender: CommandSource) {
    bean<ClientConfigHolder>().reloadFromFile()
    sender.sendText {
        appendPrefix()
        success("Cloud configuration reloaded successfully.")
    }
}