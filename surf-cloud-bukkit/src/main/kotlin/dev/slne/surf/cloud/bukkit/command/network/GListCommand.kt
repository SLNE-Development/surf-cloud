package dev.slne.surf.cloud.bukkit.command.network

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerGroupArgument
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.core.common.command.GlistCommandExecutor

fun glistCommand() = commandTree("glist") {
    withPermission(CloudPermissionRegistry.GLIST_COMMAND)

    anyExecutor { sender, _ ->
        GlistCommandExecutor.displayPlayerCount(sender)
    }

    literalArgument("all") {
        anyExecutor { sender, _ ->
            GlistCommandExecutor.displayAllOnlinePlayers(sender)
        }
    }

    literalArgument("group") {
        cloudServerGroupArgument("group") {
            anyExecutor { sender, args ->
                val group: String by args
                GlistCommandExecutor.displayOnlinePlayersInGroup(sender, group)
            }
        }
    }

    literalArgument("server") {
        cloudServerArgument("server") {
            anyExecutor { sender, args ->
                val server: CloudServer by args
                GlistCommandExecutor.displayOnlinePlayersOnServer(sender, server)
            }
        }
    }
}