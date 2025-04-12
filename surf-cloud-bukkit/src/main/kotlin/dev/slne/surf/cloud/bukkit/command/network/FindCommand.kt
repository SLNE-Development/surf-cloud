package dev.slne.surf.cloud.bukkit.command.network

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.client.paper.command.args.onlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.async
import net.kyori.adventure.text.event.ClickEvent

fun findCommand() = commandTree("find") {
    withPermission(CloudPermissionRegistry.FIND_COMMAND)
    onlineCloudPlayerArgument("player") {
        anyExecutor { sender, args ->
            val player: CloudPlayer by args

            plugin.launch {
                val serverDeferred = async { player.lastServer() }
                val displayNameDeferred = async { player.displayName() }

                val server = serverDeferred.await() ?: run {
                    sender.sendText {
                        error("Der Spielende ")
                        append(displayNameDeferred.await())
                        error(" ist nicht auf einem Server.")
                    }
                    return@launch
                }

                val displayName = displayNameDeferred.await()

                sender.sendText {
                    appendPrefix()
                    info("Der Spielende ")
                    append(displayName)
                    info(" befindet sich auf dem Server ")
                    append {
                        variableValue("${server.name} (${server.group})")
                    }

                    if (sender.hasPermission(CloudPermissionRegistry.FIND_COMMAND_TELEPORT)) {
                        hoverEvent(buildText {
                            info("Klicke, um dich zu ")
                            append(displayName)
                            info(" zu teleportieren.")
                        })

                        clickEvent(ClickEvent.callback { clicker ->
                            clicker.sendText {
                                appendPrefix()
                                info("Du wirst zu ")
                                append(displayName)
                                info(" teleportiert...")
                            }

                            plugin.launch {
                                val teleported = clicker.toCloudPlayer()?.teleport(player) == true

                                if (!teleported) {
                                    clicker.sendText {
                                        appendPrefix()
                                        error("Teleportation zu ")
                                        append(displayName)
                                        error(" fehlgeschlagen.")
                                    }
                                } else {
                                    clicker.sendText {
                                        appendPrefix()
                                        success("Du wurdest erfolgreich zu ")
                                        append(displayName)
                                        success(" teleportiert.")
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}