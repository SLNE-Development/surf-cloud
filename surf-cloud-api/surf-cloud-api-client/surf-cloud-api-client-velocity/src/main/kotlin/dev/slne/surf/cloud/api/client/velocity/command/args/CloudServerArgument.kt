package dev.slne.surf.cloud.api.client.velocity.command.args

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.surfapi.core.api.messages.adventure.text

class CloudServerArgument(nodeName: String) :
    Argument<CloudServer>(nodeName, StringArgumentType.string()) {
    override fun getPrimitiveType(): Class<CloudServer> = CloudServer::class.java
    override fun getArgumentType(): CommandAPIArgumentType = CommandAPIArgumentType.PRIMITIVE_TEXT

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection {
            CloudServerManager.retrieveAllServers()
                .filterIsInstance<CloudServer>()
                .map { it.name }
        })
    }

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): CloudServer {
        val serverName = StringArgumentType.getString(cmdCtx, key)
        val server = CloudServerManager.retrieveServerByName(serverName) as? CloudServer

        if (server == null) {
            throw SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(text("Server '$serverName' not found"))).create()
        }

        return server
    }
}

inline fun CommandTree.cloudServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(CloudServerArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.cloudServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(CloudServerArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.cloudServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(CloudServerArgument(nodeName).setOptional(optional).apply(block))