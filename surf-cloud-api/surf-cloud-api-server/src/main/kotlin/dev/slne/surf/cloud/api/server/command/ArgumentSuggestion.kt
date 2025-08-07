package dev.slne.surf.cloud.api.server.command

import com.google.common.base.CharMatcher
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

object ArgumentSuggestion {

    val MATCH_SPLITTER = CharMatcher.anyOf("._/")

    fun suggestStrings(
        strings: Iterable<String>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val remaining = builder.remainingLowerCase

        for (string in strings) {
            if (matchesSubstring(remaining, string.lowercase())) {
                builder.suggest(string)
            }
        }

        return builder.buildFuture()
    }

    fun matchesSubstring(input: String, substring: String): Boolean {
        var startIndex = 0

        while (!substring.startsWith(input, startIndex)) {
            val nextSplitIndex = MATCH_SPLITTER.indexIn(substring, startIndex)
            if (nextSplitIndex < 0) {
                return false
            }
            startIndex = nextSplitIndex + 1
        }

        return true
    }
}