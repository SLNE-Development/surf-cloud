package dev.slne.surf.cloud.standalone.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.context.ContextChain
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.slne.surf.cloud.api.server.command.CommandException
import dev.slne.surf.cloud.api.server.command.CommandResultCallback
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.core.common.coroutines.ConsoleCommandHandlerScope
import dev.slne.surf.cloud.standalone.commands.execution.ExecutionContext
import dev.slne.surf.cloud.standalone.commands.impl.ShutdownCommand
import dev.slne.surf.cloud.standalone.commands.impl.TestCommand
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.appendText
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.logger
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.ClickEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class CommandManagerImpl {

    val dispatcher = CommandDispatcher<CommandSource>()
    private val serverCommandQueue = ConcurrentLinkedQueue<ConsoleInput>()
    private lateinit var task: Job

    init {
        ShutdownCommand.register(dispatcher)
        TestCommand.register(dispatcher)
    }

    fun createCommandSource(): CommandSource {
        return CommandSource()
    }

    fun handleConsoleInput(command: String, source: CommandSource) {
        serverCommandQueue.add(ConsoleInput(command, source))
    }

    private fun handleConsoleInputs() {
        while (serverCommandQueue.isNotEmpty()) {
            val (msg, source) = serverCommandQueue.poll()
            dispatchServerCommand(source, msg)
        }
    }

    fun performCommand(
        parseresults: ParseResults<CommandSource>,
        input: String,
        label: String,
        throwOnError: Boolean = false
    ) {
        val source = parseresults.context.source
        val contextChain = finishParsing(parseresults, input, source, label) ?: return

        try {
            println("Executing command in context")
            executeCommandInContext {
                ExecutionContext.queueInitialCommandExecution(
                    it,
                    input,
                    contextChain,
                    source,
                    CommandResultCallback.EMPTY
                )
            }
        } catch (e: Throwable) {
            if (throwOnError) throw e

            log.atSevere()
                .withCause(e)
                .log("Command Exception: /$input")

            source.sendFailure(buildText {
                appendText("An unexpected error occurred trying to execute that command") {
                    hoverEvent(buildText {
                        appendText(e.message ?: e.javaClass.name)
                    })
                }
            })
        }
    }

    fun executeCommandInContext(callback: (ExecutionContext<CommandSource>) -> Unit) {
        val context: ExecutionContext<CommandSource>? = CURRENT_EXECUTION_CONTEXT.get()

        if (context == null) {
            try {
                val newContext = ExecutionContext<CommandSource>(COMMAND_CHAIN_LIMIT, FORK_LIMIT)
                CURRENT_EXECUTION_CONTEXT.set(newContext)
                callback(newContext)
                newContext.runCommandQueue()
            } finally {
                CURRENT_EXECUTION_CONTEXT.remove()
            }
        } else {
            callback(context)
        }
    }

    private fun finishParsing(
        parseresults: ParseResults<CommandSource>,
        input: String,
        source: CommandSource,
        label: String,
    ): ContextChain<CommandSource>? {
        try {
            parseresults.validateParseResults()
            return ContextChain.tryFlatten(parseresults.context.build(input)).orElseThrow {
                CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()
                    .createWithContext(parseresults.reader)
            }
        } catch (e: CommandSyntaxException) {
            source.sendFailure(buildText {
                color(Colors.ERROR)
                appendText(e.message!!)
                clickEvent(ClickEvent.suggestCommand("/$label"))
            })

            return null
        }
    }

    fun dispatchServerCommand(sender: CommandSource, serverCommand: String) {
        try {
            dispatchCommand(sender, serverCommand)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Unexpected exception while parsing console command '$serverCommand'")
        }
    }

    fun dispatchCommand(source: CommandSource, commandLine: String) {
        val results = dispatcher.parse(commandLine, source)

        try {
            performCommand(results, commandLine, commandLine, true)
        } catch (e: OutOfMemoryError) {
            throw e
        } catch (e: CommandException) {
            throw e
        } catch (e: Throwable) {
            throw CommandException("Unhandled exception executing '$commandLine'", e)
        }
    }

    private fun launchConsoleInputProcessingTask() = ConsoleCommandHandlerScope.launch {
        while (true) {
            delay(20)
            handleConsoleInputs()
        }
    }

    @Suppress("ProtectedInFinal") // IJ being dumb
    @PostConstruct
    protected fun init() {
        task = launchConsoleInputProcessingTask()
    }

    @Suppress("ProtectedInFinal") // IJ being dumb
    @PreDestroy
    protected fun destroy() = runBlocking {
        task.cancel()
        task.join()
    }

    companion object {
        private val log = logger()
        private val CURRENT_EXECUTION_CONTEXT = ThreadLocal<ExecutionContext<CommandSource>>()
        private const val COMMAND_CHAIN_LIMIT = 65536
        private const val FORK_LIMIT = 65536
    }
}

data class ConsoleInput(val msg: String, val source: CommandSource)

fun <S> ParseResults<S>.validateParseResults() {
    throw getParseException() ?: return
}

fun <S> ParseResults<S>.getParseException(): CommandSyntaxException? {
    if (!reader.canRead()) return null
    if (exceptions.size == 1) return exceptions.values.first()
    if (context.range.isEmpty) return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()
        .createWithContext(reader)
    return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument()
        .createWithContext(reader)
}