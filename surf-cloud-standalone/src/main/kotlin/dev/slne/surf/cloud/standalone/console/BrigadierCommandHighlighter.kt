package dev.slne.surf.cloud.standalone.console

import com.mojang.brigadier.tree.LiteralCommandNode
import dev.slne.surf.cloud.standalone.commands.CommandManagerImpl
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class BrigadierCommandHighlighter(private val commandManager: CommandManagerImpl) : Highlighter {
    private val commandSource by lazy { commandManager.createCommandSource() }

    companion object {
        private val colors = arrayOf(
            AttributedStyle.CYAN,
            AttributedStyle.YELLOW,
            AttributedStyle.GREEN,
            AttributedStyle.MAGENTA,
            AttributedStyle.BLUE
        )
    }

    override fun highlight(
        reader: LineReader?,
        buffer: String
    ): AttributedString? = AttributedStringBuilder().apply {
        val results = commandManager.dispatcher.parse(buffer, commandSource)
        var pos = 0
        var component = 1

        for (node in results.context.lastChild.nodes) {
            if (node.range.start >= buffer.length) break
            val start = node.range.start
            val end = node.range.end.coerceAtMost(buffer.length)

            append(buffer.substring(pos, start), AttributedStyle.DEFAULT)
            val commandNode = node.node
            if (commandNode is LiteralCommandNode<*>) {
                append(buffer.substring(start, end), AttributedStyle.DEFAULT)
            } else {
                if (component++ >= colors.size) component = 0
                append(
                    buffer.substring(start, end),
                    AttributedStyle.DEFAULT.foreground(colors[component])
                )
            }
            pos = end
        }

        if (pos < buffer.length) {
            append(buffer.substring(pos), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
        }
    }.toAttributedString()

    override fun setErrorPattern(errorPattern: Pattern?) = Unit
    override fun setErrorIndex(errorIndex: Int) = Unit
}