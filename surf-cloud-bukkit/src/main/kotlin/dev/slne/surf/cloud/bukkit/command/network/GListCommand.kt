package dev.slne.surf.cloud.bukkit.command.network

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerGroupArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.UserList
import dev.slne.surf.cloud.api.common.util.mapAsync
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.joinToComponent
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import kotlinx.coroutines.awaitAll
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import dev.slne.surf.surfapi.core.api.messages.adventure.text as component

fun glistCommand() = commandTree("glist") {
    withPermission(CloudPermissionRegistry.GLIST_COMMAND)

    anyExecutor { sender, args ->
        sender.sendText {
            appendPrefix()
            info("Es sind gerade ")
            variableValue(CloudPlayerManager.getOnlinePlayers().size)
            info(" Spielende auf dem Netzwerk online.")
        }
    }

    literalArgument("all") {
        anyExecutor { sender, args ->
            plugin.launch {
                displayOnlinePlayers(
                    sender,
                    CloudServerManager.retrieveAllServers().filterIsInstance<CloudServer>()
                )
            }
        }
    }

    literalArgument("group") {
        cloudServerGroupArgument("group") {
            anyExecutor { sender, args ->
                val group: String by args
                plugin.launch {
                    displayOnlinePlayers(
                        sender,
                        CloudServerManager.retrieveServersInGroup(group)
                            .filterIsInstance<CloudServer>()
                    )
                }
            }
        }
    }

    literalArgument("server") {
        cloudServerArgument("server") {
            anyExecutor { sender, args ->
                val server: CloudServer by args
                plugin.launch {
                    displayOnlinePlayers(sender, listOf(server))
                }
            }
        }
    }
}

private suspend fun displayOnlinePlayers(sender: CommandSender, servers: Collection<CloudServer>) {
    sender.sendText {
        appendNewPrefixedLine()
        if (servers.size == 1) {
            val server = servers.first()

            variableKey("${server.name} (")
            variableKey(server.currentPlayerCount)
            variableKey("): ")
            append(server.users.displayNames())
        } else {
            val groupedServers = servers.groupBy { it.group }.withDisplayNames()
            for ((group, serversWithName) in groupedServers) {
                if (serversWithName.isEmpty()) continue
                variableKey("$group:")
                appendNewPrefixedLine()
                for ((server, names) in serversWithName) {
                    variableKey("  ${server.name} (")
                    variableValue(server.currentPlayerCount)
                    variableKey("): ")
                    append(names)
                    appendNewPrefixedLine()
                }
            }
        }

        if (servers.size != 1) {
            val totalUsers = servers.sumOf { it.currentPlayerCount }
            appendNewPrefixedLine()
            variableKey("Insgesamt: ")
            variableValue(totalUsers)
        }
    }
}

private suspend fun Map<String, List<CloudServer>>.withDisplayNames(): Object2ObjectMap<String, List<Pair<CloudServer, Component>>> =
    mapValuesTo(mutableObject2ObjectMapOf(size)) { (_, servers) ->
        servers.mapAsync { server ->
            server to server.users.displayNames()
        }.awaitAll()
    }


private suspend fun UserList.displayNames() =
    mapAsync { it.displayName().hoverEvent(component(it.uuid, Colors.VARIABLE_VALUE)) }
        .awaitAll()
        .joinToComponent { it }