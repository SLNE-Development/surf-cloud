package dev.slne.surf.cloud.api.client.paper.command.args

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.*
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future

private val log = logger()
private val scope =
    CoroutineScope(Dispatchers.Default + CoroutineName("OnlineCloudPlayerSuggestionScope") + CoroutineExceptionHandler { coroutineContext, throwable ->
        log.atWarning()
            .withCause(throwable)
            .log("Failed to suggest online players")
    })

class OnlineCloudPlayerArgument(nodeName: String) : CustomArgument<CloudPlayer, String>(
    StringArgument(nodeName),
    { info ->
        val playerName = info.input
        CloudPlayerManager.getPlayer(playerName) ?: run {
            throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .append("Player '")
                    .appendArgInput()
                    .append("' not found")
            )
        }
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync {
            scope.future {
                CloudPlayerManager.getOnlinePlayers().map { it.name }
            }
        })
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
): CommandAPICommand = withArguments(OnlineCloudPlayerArgument(nodeName).setOptional(optional).apply(block))