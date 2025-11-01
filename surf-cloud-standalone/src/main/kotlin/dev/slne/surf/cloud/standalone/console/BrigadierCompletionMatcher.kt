package dev.slne.surf.cloud.standalone.console

import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jline.reader.Candidate
import org.jline.reader.CompletingParsedLine
import org.jline.reader.LineReader
import org.jline.reader.impl.CompletionMatcherImpl

class BrigadierCompletionMatcher : CompletionMatcherImpl() {
    override fun defaultMatchers(
        options: Map<LineReader.Option?, Boolean?>?,
        prefix: Boolean,
        line: CompletingParsedLine?,
        caseInsensitive: Boolean,
        errors: Int,
        originalGroupName: String?
    ) {
        super.defaultMatchers(options, prefix, line, caseInsensitive, errors, originalGroupName)
        matchers.addFirst { inputMap ->
            val candidates = mutableObject2ObjectMapOf<String, ObjectList<Candidate>>()

            inputMap.forEach { (key, value) ->
                if (value.all { it is BrigadierCommandCompleter.CommandCompletionCandidate }) {
                    candidates[key] = value.toObjectList()
                }
            }
            candidates
        }
    }
}