package dev.slne.surf.cloud.standalone.console

import dev.slne.surf.cloud.core.common.coroutines.ConsoleCommandInputScope
import dev.slne.surf.cloud.standalone.commands.CommandManagerImpl
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.launch
import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import kotlin.io.path.Path

@Component
class StandaloneConsole(private val commandManager: CommandManagerImpl) :
    SimpleTerminalConsole(), DisposableBean, InitializingBean {
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
//        exitProcess(ExitCodes.NORMAL)
    }

    override fun destroy() {
        running = false
    }

    override fun afterPropertiesSet() {
        ConsoleCommandInputScope.launch {
            start()
        }
    }
}