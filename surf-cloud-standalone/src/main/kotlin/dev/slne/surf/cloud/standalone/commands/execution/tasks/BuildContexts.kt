package dev.slne.surf.cloud.standalone.commands.execution.tasks

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.ContextChain
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import dev.slne.surf.cloud.api.server.command.CommandResultCallback
import dev.slne.surf.cloud.api.server.command.ExecutionCommandSource
import dev.slne.surf.cloud.standalone.commands.execution.*
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.objectListOf

open class BuildContexts<T : ExecutionCommandSource<T>>(
    private val commandInput: String,
    private val command: ContextChain<T>
) {

    protected open fun execute(
        originalSource: T,
        initialSources: List<T>,
        executionContext: ExecutionContext<T>,
        frame: Frame,
        initialModifiers: ChainModifiers
    ) {
        var currentChain = command
        var currentModifiers = initialModifiers
        var currentSources = initialSources

        if (currentChain.stage != ContextChain.Stage.EXECUTE) {
            val forkLimit = executionContext.forkLimit
            while (currentChain.stage != ContextChain.Stage.EXECUTE) {
                val topContext = currentChain.getTopContext()
                if (topContext.isForked) {
                    currentModifiers = currentModifiers.setForked()
                }

                val redirectModifier = topContext.redirectModifier

                if (redirectModifier != null) {
                    executionContext.incrementCost()
                    val isForked = currentModifiers.isForked()
                    val modifiedSources = mutableObjectListOf<T>()

                    for (source in currentSources) {
                        try {
                            val modifierResults = ContextChain.runModifier(
                                topContext,
                                source,
                                { _, _, _ -> },
                                isForked
                            )
                            if (modifiedSources.size + modifierResults.size >= forkLimit) {
                                originalSource.handleError(
                                    ERROR_FORK_LIMIT_REACHED.create(forkLimit),
                                    isForked
                                )
                                return
                            }

                            modifiedSources.addAll(modifierResults)
                        } catch (e: CommandSyntaxException) {
                            source.handleError(e, isForked)
                            if (!isForked) {
                                return
                            }
                        }
                    }

                    currentSources = modifiedSources
                }
                currentChain = currentChain.nextStage()
            }
        }

        if (currentSources.isEmpty()) {
            if (currentModifiers.isReturn()) {
                executionContext.queueNext(CommandQueueEntry(frame, FallthroughTask.instance()))
            }
        } else {
            val finalContext = currentChain.topContext

            if (currentModifiers.isReturn()) {
                val firstSource = currentSources.first().withCallback(
                    CommandResultCallback.chain(
                        currentSources.first().callback,
                        frame.resultCallback
                    )
                )
                currentSources = objectListOf(firstSource)
            }

            val executeCommand = ExecuteCommand(currentModifiers, finalContext)
            ContinuationTask.schedule(
                executionContext,
                frame,
                currentSources
            ) { nextFrame, argument ->
                CommandQueueEntry(nextFrame, executeCommand.bind(argument))
            }
        }
    }


    override fun toString(): String {
        return commandInput
    }

    class TopLevel<T : ExecutionCommandSource<T>>(
        commandInput: String,
        command: ContextChain<T>,
        private val source: T
    ) : BuildContexts<T>(commandInput, command), EntryAction<T> {
        override fun invoke(
            context: ExecutionContext<T>,
            frame: Frame
        ) {
            execute(source, objectListOf(source), context, frame, ChainModifiers.DEFAULT)
        }
    }

    companion object {
        val ERROR_FORK_LIMIT_REACHED =
            DynamicCommandExceptionType { LiteralMessage("Maximum number of contexts ($it) reached") }
    }
}