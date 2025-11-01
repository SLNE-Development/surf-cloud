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
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.messages.adventure.text

@OptIn(InternalApi::class)
class CloudServerGroupArgument(nodeName: String) :
    Argument<String>(nodeName, StringArgumentType.string()) {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getArgumentType(): CommandAPIArgumentType = CommandAPIArgumentType.PRIMITIVE_TEXT

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection {
            CommonCloudServer.all()
                .filter { it.group.isNotEmpty() }
                .map { it.group }
                .distinct()
        })
    }

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): String {
        val groupName = StringArgumentType.getString(cmdCtx, key)

        if (!CloudServerManager.existsServerGroup(groupName)) {
            throw SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(text("Server group '$groupName' not found or no servers in this group are online!"))).create()
        }

        return groupName
    }
}

inline fun CommandTree.cloudServerGroupArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(CloudServerGroupArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.cloudServerGroupArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(CloudServerGroupArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.cloudServerGroupArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(CloudServerGroupArgument(nodeName).setOptional(optional).apply(block))