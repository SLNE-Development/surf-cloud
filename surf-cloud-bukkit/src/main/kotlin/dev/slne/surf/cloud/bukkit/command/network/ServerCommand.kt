package dev.slne.surf.cloud.bukkit.command.network

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.cloud.api.client.paper.command.args.cloudServerArgument
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum.*
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun serverCommand() = commandTree("server") {
    withPermission(CloudPermissionRegistry.SERVER_COMMAND)
    cloudServerArgument("server") {
        playerExecutor { sender, args ->
            val server: CloudServer by args
            val cloudPlayer = sender.toCloudPlayer() ?: return@playerExecutor

            if (cloudPlayer.isOnServer(server)) {
                throw CustomArgumentException.fromString("Du befindest dich bereits auf dem Server!")
            }

            if (!sender.hasPermission(CloudPermissionRegistry.ALL_SERVER_PERMISSION)
                && !sender.hasPermission(CloudPermissionRegistry.SPECIFIC_SERVER_PERMISSION_PREFIX + server.name)
            ) {
                throw CustomArgumentException.fromString("Du hast keine Berechtigung, um auf diesen Server zu wechseln!")
            }

            sender.sendText {
                appendPrefix()
                info("Du wirst mit dem Server ")
                append {
                    variableValue("${server.name} (${server.group})")
                }
                info(" verbunden...")
            }

            plugin.launch {
                val result = cloudPlayer.connectToServer(server)

                sender.sendText {
                    appendPrefix()
                    when (result) {
                        SUCCESS -> append {
                            success("Du wurdest erfolgreich mit dem Server ")
                            variableValue("${server.name} (${server.group})")
                            success(" verbunden.")
                        }

                        SERVER_FULL, CATEGORY_FULL -> append {
                            error("Der Server ")
                            variableValue("${server.name} (${server.group})")
                            error(" ist voll.")
                        }

                        SERVER_OFFLINE -> append {
                            error("Der Server ")
                            variableValue("${server.name} (${server.group})")
                            error(" ist offline.")
                        }

                        CANNOT_SWITCH_PROXY -> append {
                            error("Der Server ")
                            variableValue("${server.name} (${server.group})")
                            error(" befindet sich unter einem anderen Proxy.")
                        }

                        CANNOT_COMMUNICATE_WITH_PROXY -> append {
                            error("Der Server ")
                            variableValue("${server.name} (${server.group})")
                            error(" kann nicht mit dem Proxy kommunizieren.")
                        }

                        CONNECTION_IN_PROGRESS -> append {
                            error("Du wechselst bereits einen Server.")
                        }

                        CONNECTION_CANCELLED -> append {
                            error("Die Verbindung wurde abgebrochen.")
                        }

                        SERVER_DISCONNECTED -> append {
                            error("Die Verbindung zum Server ")
                            variableValue("${server.name} (${server.group})")
                            error(" wurde getrennt.")
                        }

                        is SERVER_NOT_FOUND -> throw AssertionError("Server not found")
                        ALREADY_CONNECTED -> throw AssertionError("Already connected")
                        OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET -> throw AssertionError()
                        CANNOT_CONNECT_TO_PROXY -> throw AssertionError("Cannot connect to proxy")
                        else -> throw AssertionError("Unknown error: $result")
                    }
                }
            }
        }
    }
}