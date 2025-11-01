package dev.slne.surf.cloud.api.client.paper.command.args

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future


@OptIn(InternalApi::class)
class CloudServerArgument(nodeName: String) : CustomArgument<CloudServer, String>(
    StringArgument(nodeName),
    { info ->
        val serverName = info.input
        val server = CloudServerManager.retrieveServerByName(serverName) as? CloudServer ?: run {
            throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .append("Server '")
                    .appendArgInput()
                    .append("' not found")
            )
        }
        server
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync {
            scope.future {
                CloudServerManager.retrieveAllServers()
                    .filterIsInstance<CloudServer>()
                    .map { it.name }
            }
        })
    }

    private companion object {
        private val log = logger()
        private val scope =
            CoroutineScope(Dispatchers.Default + CoroutineName("CloudServerSuggestionScope") + CoroutineExceptionHandler { coroutineContext, throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("Failed to suggest cloud servers")
            })
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
): CommandAPICommand =
    withArguments(CloudServerArgument(nodeName).setOptional(optional).apply(block))