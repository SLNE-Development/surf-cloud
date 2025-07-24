package dev.slne.surf.cloud.api.client.velocity.command.args

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import dev.jorel.commandapi.Brigadier
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.cloud.api.client.velocity.InternalCloudVelocityBridge
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class OfflineCloudPlayerArgument(nodeName: String) :
    Argument<Deferred<OfflineCloudPlayer?>>(nodeName, StringArgumentType.word()) {

    @Suppress("UNCHECKED_CAST")
    override fun getPrimitiveType(): Class<Deferred<OfflineCloudPlayer?>> =
        Deferred::class.java as Class<Deferred<OfflineCloudPlayer?>>

    override fun getArgumentType(): CommandAPIArgumentType = CommandAPIArgumentType.PRIMITIVE_STRING

    @OptIn(InternalApi::class)
    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): Deferred<OfflineCloudPlayer?> {
        val playerName = StringArgumentType.getString(cmdCtx, key)
        val onlinePlayer = InternalCloudVelocityBridge.instance.getPlayer(playerName)

        if (onlinePlayer != null) {
            return CompletableDeferred(onlinePlayer.toOfflineCloudPlayer())
        }

        val deferred = CompletableDeferred<OfflineCloudPlayer?>()
        InternalCloudVelocityBridge.instance.launch {
            val uuid = PlayerLookupService.getUuid(playerName)
            val cloudPlayer = uuid.toOfflineCloudPlayer()

            if (cloudPlayer == null) {
                val sender = Brigadier.getCommandSenderFromContext<CommandSource>(cmdCtx)
                sender.sendText { error("Player with name '$playerName' not found.") }
            }

            deferred.complete(cloudPlayer)
        }

        return deferred
    }
}

inline fun CommandTree.offlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(OfflineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.offlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(OfflineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.offlineCloudPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(OfflineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))