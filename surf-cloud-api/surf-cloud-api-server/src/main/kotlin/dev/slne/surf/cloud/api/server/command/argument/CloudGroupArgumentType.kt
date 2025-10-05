package dev.slne.surf.cloud.api.server.command.argument

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.server.command.ArgumentSuggestion
import java.util.concurrent.CompletableFuture

class CloudGroupArgumentType private constructor(): ArgumentType<String> {
    companion object {
        val GROUP_NOT_FOUND =
            DynamicCommandExceptionType { group -> LiteralMessage("Server group '$group' not found or no servers in this group are online!") }

        fun group(): CloudGroupArgumentType {
            return CloudGroupArgumentType()
        }

        fun getGroup(context: CommandContext<*>, name: String): String {
            return context.getArgument(name, String::class.java)
        }
    }

    override fun parse(reader: StringReader): String {
        val group = reader.readUnquotedString()

        if (!CloudServerManager.existsServerGroup(group)) {
            throw GROUP_NOT_FOUND.create(group)
        }

        return group
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val groups = CloudServerManager.retrieveAllServers()
            .filter { it.group.isNotEmpty() }
            .map { it.group }
            .distinct()

        return ArgumentSuggestion.suggestStrings(groups, builder)
    }
}