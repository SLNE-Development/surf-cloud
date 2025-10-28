package dev.slne.surf.cloud.api.server.command.argument

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.server.command.ArgumentSuggestion
import java.util.concurrent.CompletableFuture

class CloudPlayerArgumentType private constructor() : ArgumentType<CloudPlayer> {

    companion object {
        val NO_PLAYER_FOUND = SimpleCommandExceptionType(LiteralMessage("No player was found"))

        fun player(): CloudPlayerArgumentType {
            return CloudPlayerArgumentType()
        }

        fun getPlayer(context: CommandContext<*>, name: String): CloudPlayer {
            return context.getArgument(name, CloudPlayer::class.java)
        }
    }

    override fun parse(reader: StringReader): CloudPlayer {
        val playerName = reader.readUnquotedString()
        val player = CloudPlayer[playerName]

        if (player == null) {
            throw NO_PLAYER_FOUND.create()
        }

        return player
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val playerNames = CloudPlayer.all().map(CloudPlayer::name)
        return ArgumentSuggestion.suggestStrings(playerNames, builder)
    }
}