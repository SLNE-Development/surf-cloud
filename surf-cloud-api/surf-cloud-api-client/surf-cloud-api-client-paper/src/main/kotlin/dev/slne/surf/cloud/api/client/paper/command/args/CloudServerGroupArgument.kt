package dev.slne.surf.cloud.api.client.paper.command.args

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.TextArgument
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future

@OptIn(InternalApi::class)
class CloudServerGroupArgument(nodeName: String) : CustomArgument<String, String>(
    TextArgument(nodeName),
    { info ->
        val groupName = info.currentInput
        val exists = CloudServerManager.existsServerGroup(groupName)
        if (!exists) {
            throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .append("Server group '")
                    .appendArgInput()
                    .append("' not found or no servers in this group are online!")
            )
        }
        groupName
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync {
            scope.future {
                CloudServerManager.retrieveAllServers()
                    .filter { it.group.isNotEmpty() }
                    .map { it.group }
                    .distinct()
            }
        })
    }

    companion object {
        private val log = logger()
        private val scope =
            CoroutineScope(Dispatchers.Default + CoroutineName("CloudServerGroupSuggestionScope") + CoroutineExceptionHandler { coroutineContext, throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("Failed to suggest cloud server groups")
            })
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
): CommandAPICommand = withArguments(CloudServerGroupArgument(nodeName).setOptional(optional).apply(block))