package dev.slne.surf.cloud.api.client.velocity.command.args

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.surfapi.core.api.messages.adventure.text

class OnlineCloudPlayerArgument(nodeName: String) :
    Argument<CloudPlayer>(nodeName, StringArgumentType.word()) {
    override fun getPrimitiveType(): Class<CloudPlayer> = CloudPlayer::class.java
    override fun getArgumentType(): CommandAPIArgumentType = CommandAPIArgumentType.PRIMITIVE_STRING

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): CloudPlayer {
        val playerName = StringArgumentType.getString(cmdCtx, key)
        val player = CloudPlayerManager.getPlayer(playerName)

        if (player == null) {
            throw SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(text("Player '$playerName' not found"))).create()
        }

        return player
    }
}

inline fun CommandTree.onlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(OnlineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.onlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(OnlineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.onlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(OnlineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))