package dev.slne.surf.cloud.api.client.paper.command.args

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument
import dev.jorel.commandapi.arguments.CustomArgument
import dev.slne.surf.cloud.api.client.paper.player.toCloudOfflinePlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

class OfflineCloudPlayerArgument(nodeName: String) :
    CustomArgument<Deferred<OfflineCloudPlayer?>, CompletableFuture<OfflinePlayer>>(
        AsyncOfflinePlayerArgument(nodeName),
        { info ->
            scope.async {
                try {
                    val player = info.currentInput.await()
                    player.toCloudOfflinePlayer()
                } catch (e: RuntimeException) {
                    val cause = e.cause
                    val rootCause = if (cause is RuntimeException) cause.cause else cause

                    info.sender.sendText {
                        error(
                            rootCause?.message
                                ?: "Unknown error occurred while fetching offline player"
                        )
                    }
                    null
                }
            }
        }
    ) {
    init {
        includeSuggestions(ArgumentSuggestions.stringCollectionAsync {
            scope.future {
                CloudPlayerManager.getOnlinePlayers().map { it.name }
            }
        })
    }

    companion object {
        private val log = logger()
        private val scope =
            CoroutineScope(Dispatchers.IO + CoroutineName("OfflineCloudPlayerArgument") + CoroutineExceptionHandler { _, throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("An error occurred in OfflineCloudPlayerArgument")
            })
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