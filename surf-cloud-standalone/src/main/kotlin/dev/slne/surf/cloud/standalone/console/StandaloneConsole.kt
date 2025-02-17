package dev.slne.surf.cloud.standalone.console

import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.coroutines.ConsoleCommandInputScope
import dev.slne.surf.cloud.standalone.commands.CommandManagerImpl
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.launch
import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.io.path.Path
import kotlin.system.exitProcess

@Component
class StandaloneConsole(private val commandManager: CommandManagerImpl) :
    SimpleTerminalConsole() {
    @Volatile
    private var running = true

    override fun buildReader(builder: LineReaderBuilder): LineReader? = builder.run {
        appName("SurfCloud")
        variable(LineReader.HISTORY_FILE, Path(".console_history"))
        completer(BrigadierCommandCompleter(commandManager))
        option(LineReader.Option.COMPLETE_IN_WORD, true)
        highlighter(BrigadierCommandHighlighter(commandManager))

        System.setProperty("org.jline.reader.support.parsedline", "true")
        parser(BrigadierConsoleParser(commandManager))
        completionMatcher(BrigadierCompletionMatcher())

        super.buildReader(builder)
    }


    override fun isRunning(): Boolean {
        return running
    }

    override fun runCommand(command: String) {
        commandManager.handleConsoleInput(command, commandManager.createCommandSource())
    }

    override fun shutdown() {
        exitProcess(ExitCodes.NORMAL)
    }

    @Suppress("ProtectedInFinal") // IJ being dumb
    @EventListener(ContextClosedEvent::class)
    protected fun onContextClose() {
        running = false
        logger().atInfo().log("Shutting down console")
    }

    @Suppress("ProtectedInFinal") // IJ being dumb
    @PostConstruct
    protected fun init() {
        ConsoleCommandInputScope.launch {
            start()
        }
    }
}