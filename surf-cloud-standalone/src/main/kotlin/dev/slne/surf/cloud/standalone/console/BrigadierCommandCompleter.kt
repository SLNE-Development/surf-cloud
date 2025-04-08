package dev.slne.surf.cloud.standalone.console

import com.mojang.brigadier.Message
import com.mojang.brigadier.suggestion.Suggestion
import dev.slne.surf.cloud.api.server.command.CommandCompletion
import dev.slne.surf.cloud.standalone.commands.CommandManagerImpl
import dev.slne.surf.surfapi.core.api.messages.ComponentMessage
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class BrigadierCommandCompleter(private val commandManager: CommandManagerImpl): Completer {
    private val commandSource by lazy { commandManager.createCommandSource() }

    override fun complete(
        reader: LineReader,
        line: ParsedLine,
        candidates: MutableList<Candidate>
    ) = runBlocking {
        complete(reader, line, candidates, emptyList())
    }

    suspend fun complete(
        reader: LineReader,
        line: ParsedLine,
        candidates: MutableList<Candidate>,
        existing: List<CommandCompletion>
    ) {
        val dispatcher = commandManager.dispatcher
        val results = dispatcher.parse(line.line(), commandSource)
        addCandidates(
            candidates,
            dispatcher.getCompletionSuggestions(results).await().list,
            existing,
            ParseContext(line.line(), results.context.findSuggestionContext(line.cursor()).startPos)
        )
    }

    private fun addCandidates(
        candidates: MutableList<Candidate>,
        brigSuggestion: List<Suggestion>,
        existing: List<CommandCompletion>,
        context: ParseContext
    ) {
        for (brigSuggestion in brigSuggestion) {
            if (brigSuggestion.text.isEmpty()) continue
            candidates.add(brigSuggestion.toCandidate(context))
        }

        for (completion in existing) {
            if (completion.suggestion.isEmpty() || brigSuggestion.any { it.text == completion.suggestion }) continue
            candidates.add(toCandidate(completion.suggestion, completion.tooltip))
        }
    }

    private data class ParseContext(
        val line: String,
        val suggestionStart: Int,
    )

    private fun Suggestion.toCandidate(context: ParseContext): Candidate {
        var tooltipComponent: Component? = null
        val suggestedTooltip: Message? = tooltip
        if (suggestedTooltip != null) {
            tooltipComponent =
                if (suggestedTooltip is ComponentMessage) suggestedTooltip.asComponent()
                else Component.text(suggestedTooltip.string)
        }


        return toCandidate(
            context.line.substring(context.suggestionStart, range.start) + text,
            tooltipComponent
        )
    }

    private fun toCandidate(suggestionText: String, tooltip: Component?): Candidate {
        val suggestionTooltip = ANSIComponentSerializer.ansi().serializeOr(tooltip, null)
        return CommandCompletionCandidate(
            suggestionText,
            suggestionText,
            null,
            suggestionTooltip,
            null,
            null,
            false
        )
    }

    class CommandCompletionCandidate(
        value: String,
        display: String,
        group: String?,
        descr: String?,
        suffix: String?,
        key: String?,
        complete: Boolean
    ) : Candidate(value, display, group, descr, suffix, key, complete)
}