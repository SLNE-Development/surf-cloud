package dev.slne.surf.cloud.core.common.command

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.UserList
import dev.slne.surf.cloud.api.common.util.mapAsync
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.core.common.coroutines.CommandExecutionScope
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.joinToComponent
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import dev.slne.surf.surfapi.core.api.messages.adventure.text as component

object GlistCommandExecutor {

    fun displayPlayerCount(sender: Audience) {
        sender.sendText {
            appendPrefix()
            info("Es sind gerade ")
            variableValue(CloudPlayerManager.getOnlinePlayers().size)
            info(" Nutzer auf dem Netzwerk online.")
        }
    }

    fun displayAllOnlinePlayers(sender: Audience) {
        CommandExecutionScope.launch {
            displayOnlinePlayers(sender, CloudServer.all())
        }
    }

    fun displayOnlinePlayersInGroup(sender: Audience, group: String) {
        CommandExecutionScope.launch {
            displayOnlinePlayers(sender, CloudServer.inGroup(group))
        }
    }

    fun displayOnlinePlayersOnServer(sender: Audience, server: CloudServer) {
        CommandExecutionScope.launch {
            displayOnlinePlayers(sender, listOf(server))
        }
    }

    private suspend fun displayOnlinePlayers(sender: Audience, servers: Collection<CloudServer>) {
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
}