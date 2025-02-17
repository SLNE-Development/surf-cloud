package dev.slne.surf.cloud.standalone.commands.execution.tasks

import dev.slne.surf.cloud.standalone.commands.execution.CommandQueueEntry
import dev.slne.surf.cloud.standalone.commands.execution.EntryAction
import dev.slne.surf.cloud.standalone.commands.execution.ExecutionContext
import dev.slne.surf.cloud.standalone.commands.execution.Frame


class ContinuationTask<T, P> private constructor(
    private val taskFactory: TaskProvider<T, P>,
    private val arguments: List<P>,
    frame: Frame
) : EntryAction<T> {
    private val selfEntry = CommandQueueEntry(frame, this)
    private var index = 0

    override fun invoke(context: ExecutionContext<T>, frame: Frame) {
        val arg = arguments[index]
        context.queueNext(taskFactory(frame, arg))

        if (index++ < arguments.size) {
            context.queueNext(selfEntry)
        }
    }

    companion object {
        fun <T, P> schedule(
            executionContext: ExecutionContext<T>,
            frame: Frame,
            arguments: List<P>,
            taskProvider: TaskProvider<T, P>
        ) {
            when (arguments.size) {
                0 -> {}
                1 -> executionContext.queueNext(taskProvider(frame, arguments[0]))
                2 -> {
                    executionContext.queueNext(taskProvider(frame, arguments[0]))
                    executionContext.queueNext(taskProvider(frame, arguments[1]))
                }

                else -> executionContext.queueNext(
                    ContinuationTask(
                        taskProvider,
                        arguments,
                        frame
                    ).selfEntry
                )
            }
        }
    }

}

typealias TaskProvider<T, P> = (Frame, P) -> CommandQueueEntry<T>