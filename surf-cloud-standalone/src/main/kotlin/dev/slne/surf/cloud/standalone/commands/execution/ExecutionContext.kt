package dev.slne.surf.cloud.standalone.commands.execution

import com.mojang.brigadier.context.ContextChain
import dev.slne.surf.cloud.api.server.command.CommandResultCallback
import dev.slne.surf.cloud.api.server.command.ExecutionCommandSource
import dev.slne.surf.cloud.standalone.commands.execution.tasks.BuildContexts
import dev.slne.surf.surfapi.core.api.util.logger

class ExecutionContext<T>(
    private val commandLimit: Int,
    val forkLimit: Int,
    private var commandQuota: Int = commandLimit,
) {
    private val commandQueue = ArrayDeque<CommandQueueEntry<T>>()
    private val newTopCommands = mutableListOf<CommandQueueEntry<T>>()

    private var queueOverflow = false
    private var currentFrameDepth = 0

    private fun handleQueueOverflow() {
        queueOverflow = true
        commandQueue.clear()
        newTopCommands.clear()
    }

    fun queueNext(entry: CommandQueueEntry<T>) {
        if (newTopCommands.size + commandQueue.size > MAX_QUEUE_DEPTH) {
            handleQueueOverflow()
        }

        if (!queueOverflow) {
            newTopCommands.add(entry)
        }
    }

    fun discardAtDepthOrHigher(depth: Int) {
        while (commandQueue.isNotEmpty() && commandQueue.first().frame.depth >= depth) {
            commandQueue.removeFirst()
        }
    }

    fun frameControlForDepth(depth: Int): FrameControl = { discardAtDepthOrHigher(depth) }

    fun runCommandQueue() {
        pushNewCommands()

        while (true) {
            if (commandQuota <= 0) {
                log.atInfo()
                    .log("Command quota exhausted, stopping execution (executed $commandLimit commands)")
                break
            }

            val entry = commandQueue.removeFirstOrNull() ?: return
            currentFrameDepth = entry.frame.depth
            entry.execute(this)

            if (queueOverflow) {
                log.atInfo()
                    .log("Queue overflow detected, stopping execution (max $MAX_QUEUE_DEPTH commands)")
                break
            }

            pushNewCommands()
        }

        currentFrameDepth = 0
    }

    private fun pushNewCommands() {
        commandQueue.addAll(newTopCommands)
        newTopCommands.clear()
    }

    fun incrementCost() {
        commandQuota--
    }

    companion object {
        private const val MAX_QUEUE_DEPTH = 10_000_000
        private val log = logger()

        fun <T : ExecutionCommandSource<T>> queueInitialCommandExecution(
            context: ExecutionContext<T>,
            commandInput: String,
            command: ContextChain<T>,
            source: T,
            callback: CommandResultCallback,
        ) {
            context.queueNext(
                CommandQueueEntry(
                    context.createTopFrame(callback),
                    BuildContexts.TopLevel(commandInput, command, source)
                )
            )
        }

        private fun <T : ExecutionCommandSource<T>> ExecutionContext<T>.createTopFrame(callback: CommandResultCallback): Frame {
            return if (currentFrameDepth == 0) {
                Frame(0, callback) { commandQueue.clear() }
            } else {
                val frameDepth = currentFrameDepth + 1
                Frame(frameDepth, callback, frameControlForDepth(frameDepth))
            }
        }
    }
}

