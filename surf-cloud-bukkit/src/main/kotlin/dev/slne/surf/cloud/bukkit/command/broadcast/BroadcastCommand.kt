package dev.slne.surf.cloud.bukkit.command.broadcast

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerArgument
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerGroupArgument
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.bukkit.api.command.args.MiniMessageArgument
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import java.util.function.BiFunction

@Suppress("DuplicatedCode")
fun broadcastCommand() = commandTree("broadcast") {
    withPermission(CloudPermissionRegistry.BROADCAST_COMMAND)

    literalArgument("--server") {
        cloudServerArgument("server") {
            literalArgument("--message") {
                argument(MiniMessageArgument("message")) {
                    anyExecutor { sender, args ->
                        val server: CloudServer by args
                        val message: Component by args
                        executeBroadcast(sender, message, server = server)
                    }
                }
            }
            literalArgument("--prefix") {
                argument(MiniMessageArgument("prefix")) {
                    literalArgument("--message") {
                        argument(MiniMessageArgument("message")) {
                            anyExecutor { sender, args ->
                                val server: CloudServer by args
                                val prefix: Component by args
                                val message: Component by args
                                executeBroadcast(sender, message, prefix = prefix, server = server)
                            }
                        }
                    }
                }
            }
        }
    }

    literalArgument("--group") {
        cloudServerGroupArgument("group") {
            literalArgument("--message") {
                argument(MiniMessageArgument("message")) {
                    anyExecutor { sender, args ->
                        val group: String by args
                        val message: Component by args
                        executeBroadcast(sender, message, group = group)
                    }
                }
            }
            literalArgument("--prefix") {
                argument(MiniMessageArgument("prefix")) {
                    literalArgument("--message") {
                        argument(MiniMessageArgument("message")) {
                            anyExecutor { sender, args ->
                                val group: String by args
                                val prefix: Component by args
                                val message: Component by args
                                executeBroadcast(sender, message, prefix = prefix, group = group)
                            }
                        }
                    }
                }
            }
        }
    }

    literalArgument("--prefix") {
        argument(MiniMessageArgument("prefix")) {
            literalArgument("--message") {
                argument(MiniMessageArgument("message")) {
                    anyExecutor { sender, args ->
                        val prefix: Component by args
                        val message: Component by args
                        executeBroadcast(sender, message, prefix = prefix)
                    }
                }
            }
            literalArgument("--server") {
                cloudServerArgument("server") {
                    literalArgument("--message") {
                        argument(MiniMessageArgument("message")) {
                            anyExecutor { sender, args ->
                                val server: CloudServer by args
                                val prefix: Component by args
                                val message: Component by args
                                executeBroadcast(sender, message, prefix = prefix, server = server)
                            }
                        }
                    }
                }
            }
            literalArgument("--group") {
                cloudServerGroupArgument("group") {
                    literalArgument("--message") {
                        argument(MiniMessageArgument("message")) {
                            anyExecutor { sender, args ->
                                val group: String by args
                                val prefix: Component by args
                                val message: Component by args
                                executeBroadcast(sender, message, prefix = prefix, group = group)
                            }
                        }
                    }
                }
            }
        }
    }

    literalArgument("--message") {
        argument(MiniMessageArgument("message")) {
            anyExecutor { sender, args ->
                val message: Component by args
                executeBroadcast(sender, message)
            }
        }
    }
}

private fun executeBroadcast(
    sender: CommandSender,
    message: Component,
    prefix: Component? = null,
    server: CloudServer? = null,
    group: String? = null
) = plugin.launch{
    val prefix = prefix ?: Colors.PREFIX
    val message = message.replaceText {
        it.match("(?m)^|\\A")
            .replacement(prefix)
            .replaceInsideHoverEvents(false)
    }

    launch {
        when {
            server != null -> server.broadcast(message)
            group != null -> CloudServerManager.broadcastToGroup(group, message)
            else -> CloudServerManager.broadcast(message)
        }
    }

    sender.sendText {
        appendPrefix()
        success("Die Nachricht wurde erfolgreich an ")
        if (server != null) {
            success("den Server ")
            variableValue(server.name)
        } else if (group != null) {
            success("die Gruppe ")
            variableValue(group)
        } else {
            success("alle")
        }
        success(" gesendet.")
    }
}