package dev.slne.surf.cloud.standalone.console

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContextBuilder
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.util.contains
import dev.slne.surf.cloud.standalone.commands.CommandManagerImpl
import org.jline.reader.ParsedLine
import org.jline.reader.Parser

class BrigadierConsoleParser(private val commandManager: CommandManagerImpl) : Parser {

    override fun parse(
        line: String?,
        cursor: Int,
        context: Parser.ParseContext?
    ): ParsedLine? {
        val results = commandManager.dispatcher.parse(
            StringReader(line),
            commandManager.createCommandSource()
        )
        val reader = results.reader
        val words = mutableObjectListOf<String>()
        var currentContext: CommandContextBuilder<CommandSource>? = results.context
        var currentWordIdx = -1
        var wordIdx = -1
        var inWordCursor = -1

        if (currentContext!!.range.length > 0) {
            while (currentContext != null) {
                for (node in currentContext.nodes) {
                    val nodeRange = node.range
                    val current = nodeRange.get(reader)
                    words.add(current)
                    currentWordIdx++

                    if (wordIdx == -1 && cursor in nodeRange) {
                        wordIdx = currentWordIdx
                        inWordCursor = cursor - nodeRange.start
                    }
                }
                currentContext = currentContext.child
            }
        }

        val leftovers = reader.remaining
        if (leftovers.isNotEmpty() && leftovers.isBlank()) {
            currentWordIdx++
            words.add("")
            if (wordIdx == -1) {
                wordIdx = currentWordIdx
                inWordCursor = 0
            }
        } else if (leftovers.isNotEmpty()) {
            currentWordIdx++
            words.add(leftovers)
            if (wordIdx == -1) {
                wordIdx = currentWordIdx
                inWordCursor = cursor - reader.cursor
            }
        }

        if (wordIdx == -1) {
            currentWordIdx++
            words.add("")
            wordIdx = currentWordIdx
            inWordCursor = 0
        }

        return BrigadierParsedLine(
            words[wordIdx],
            inWordCursor,
            wordIdx,
            words,
            line,
            cursor
        )
    }

    data class BrigadierParsedLine(
        val word: String,
        val wordCursor: Int,
        val wordIndex: Int,
        val words: List<String>,
        val line: String?,
        val cursor: Int
    ) : ParsedLine {
        override fun word() = word
        override fun wordCursor() = wordCursor
        override fun wordIndex() = wordIndex
        override fun words() = words
        override fun line() = line
        override fun cursor() = cursor
    }
}