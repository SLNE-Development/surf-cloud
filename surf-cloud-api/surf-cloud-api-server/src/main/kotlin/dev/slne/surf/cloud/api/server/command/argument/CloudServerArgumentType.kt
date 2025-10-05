package dev.slne.surf.cloud.api.server.command.argument

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.server.command.ArgumentSuggestion
import java.util.concurrent.CompletableFuture

class CloudServerArgumentType private constructor() : ArgumentType<CloudServer> {
    companion object {
        val SERVER_NOT_FOUND =
            DynamicCommandExceptionType { group -> LiteralMessage("Server '$group' not found!") }

        fun server(): CloudServerArgumentType {
            return CloudServerArgumentType()
        }

        fun getServer(context: CommandContext<*>, name: String): CloudServer {
            return context.getArgument(name, CloudServer::class.java)
        }
    }

    override fun parse(reader: StringReader): CloudServer {
        val serverName = reader.readUnquotedString()
        val server = CloudServer[serverName]

        if (server == null) {
            throw SERVER_NOT_FOUND.create(serverName)
        }

        return server
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val serverNames = CloudServer.all().map(CloudServer::name)
        return ArgumentSuggestion.suggestStrings(serverNames, builder)
    }
}