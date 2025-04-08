package dev.slne.surf.cloud.standalone.commands.execution

import dev.slne.surf.cloud.api.server.command.CommandResultCallback

data class CommandQueueEntry<T>(val frame: Frame, val action: EntryAction<T>) {
    fun execute(context: ExecutionContext<T>) = action(context, frame)
}

data class Frame(
    val depth: Int,
    val resultCallback: CommandResultCallback,
    val frameControl: FrameControl
) {
    fun returnSuccess(result: Int) = resultCallback.onSuccess(result)
    fun returnFailure() = resultCallback.onFailure()
    fun discard() = frameControl()
}

typealias FrameControl = () -> Unit

fun interface EntryAction<T> {
    operator fun invoke(context: ExecutionContext<T>, frame: Frame)
}

fun interface UnboundEntryAction<T> {
    fun execute(source: T, executionContext: ExecutionContext<T>, frame: Frame)
    fun bind(source: T) = EntryAction { context, frame -> this.execute(source, context, frame) }
}
