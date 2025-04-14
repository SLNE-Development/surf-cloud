package dev.slne.surf.cloud.bukkit.command.network

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerGroupArgument
import dev.slne.surf.cloud.api.client.paper.command.args.onlineCloudPlayerArgument
import dev.slne.surf.cloud.api.client.server.CloudClientServerManager
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import it.unimi.dsi.fastutil.objects.ObjectList
import org.bukkit.command.CommandSender

fun sendCommand() = commandTree("send") {
    literalArgument("player") {
        onlineCloudPlayerArgument("player") {
            literalArgument("toServer") {
                cloudServerArgument("server") {
                    anyExecutor { sender, args ->
                        val player: CloudPlayer by args
                        val server: CloudServer by args
                        sendPlayersToServer(sender, server, listOf(player))
                    }
                }
            }

            literalArgument("toGroup") {
                cloudServerGroupArgument("group") {
                    anyExecutor { sender, args ->
                        val player: CloudPlayer by args
                        val group: String by args
                        sendPlayersToGroup(sender, group, listOf(player))
                    }
                }
            }
        }
    }

    literalArgument("all") {
        literalArgument("toServer") {
            cloudServerArgument("server") {
                anyExecutor { sender, args ->
                    val server: CloudServer by args
                    val players = CloudPlayerManager.getOnlinePlayers()
                        .filterNot { it.isOnServer(server) }
                    sendPlayersToServer(sender, server, players)
                }
            }
        }
        literalArgument("toGroup") {
            cloudServerGroupArgument("group") {
                anyExecutor { sender, args ->
                    val group: String by args
                    val players = CloudPlayerManager.getOnlinePlayers()
                        .filterNot { it.isInGroup(group) }
                    sendPlayersToGroup(sender, group, players)
                }
            }
        }
    }

    literalArgument("current") {
        literalArgument("toServer") {
            cloudServerArgument("server") {
                anyExecutor { sender, args ->
                    val server: CloudServer by args
                    val current = CloudClientServerManager.currentServer()

                    if (current == server) {
                        throw CommandAPI.failWithString("Cannot send players to the same server.")
                    }

                    sendPlayersToServer(sender, server, current.users)
                }
            }
        }
        literalArgument("toGroup") {
            cloudServerGroupArgument("group") {
                anyExecutor { sender, args ->
                    val group: String by args
                    val current = CloudClientServerManager.currentServer()

                    if (current.isInGroup(group)) {
                        throw CommandAPI.failWithString("Cannot send players to the same group.")
                    }

                    sendPlayersToGroup(sender, group, current.users)
                }
            }
        }
    }

    literalArgument("server") {
        cloudServerArgument("from") {
            literalArgument("toServer") {
                cloudServerArgument("to") {
                    anyExecutor { sender, args ->
                        val from: CloudServer by args
                        val to: CloudServer by args

                        sendPlayersToServer(sender, to, from.users)
                    }
                }
            }
            literalArgument("toGroup") {
                cloudServerGroupArgument("group") {
                    anyExecutor { sender, args ->
                        val server: CloudServer by args
                        val group: String by args

                        sendPlayersToGroup(sender, group, server.users)
                    }
                }
            }
        }
    }

    literalArgument("group") {
        cloudServerGroupArgument("from") {
            literalArgument("toServer") {
                cloudServerArgument("to") {
                    anyExecutor { sender, args ->
                        val from: String by args
                        val to: CloudServer by args

                        sendPlayersToServer(
                            sender,
                            to,
                            CloudPlayerManager.getOnlinePlayers().filter { it.isInGroup(from) })
                    }
                }
            }
            literalArgument("toGroup") {
                cloudServerGroupArgument("group") {
                    anyExecutor { sender, args ->
                        val from: String by args
                        val group: String by args

                        if (from.equals(group, ignoreCase = true)) {
                            throw CommandAPI.failWithString("Cannot send players to the same group.")
                        }

                        sendPlayersToGroup(
                            sender,
                            group,
                            CloudPlayerManager.getOnlinePlayers().filter { it.isInGroup(from) })
                    }
                }
            }
        }
    }
}

private fun sendPlayersToServer(
    sender: CommandSender,
    server: CloudServer,
    players: Collection<CloudPlayer>
) = plugin.launch {
    sender.sendText {
        appendPrefix()
        variableValue(players.size)
        info(" Spielende werden verschickt...")
    }

    val results = server.pullPlayers(players)
    handleSendResults(sender, players, results)
}

private fun sendPlayersToGroup(
    sender: CommandSender,
    group: String,
    players: Collection<CloudPlayer>
) = plugin.launch {
    sender.sendText {
        appendPrefix()
        variableValue(players.size)
        info(" Spielende werden verschickt...")
    }

    val results = CloudServerManager.pullPlayersToGroup(group, players)
    handleSendResults(sender, players, results)
}

private fun handleSendResults(
    sender: CommandSender,
    players: Collection<CloudPlayer>,
    results: ObjectList<Pair<CloudPlayer, ConnectionResultEnum>>
) {
    var count = 0
    results.forEach { (player, result) ->
        if (result.isSuccess) {
            count++
        } else {
            player.sendMessage(result.message)
        }
    }

    if (count == 0) {
        sender.sendText {
            appendPrefix()
            error("Niemand wurde verschickt.")
        }
    } else if (count < players.size) {
        sender.sendText {
            appendPrefix()
            error("Nur $count/${players.size} Spielende wurden verschickt.")
        }
    } else {
        sender.sendText {
            appendPrefix()
            success("Alle ${players.size} Spielenden wurden verschickt.")
        }
    }
}
