package dev.slne.surf.cloud.standalone.commands.execution.tasks

import dev.slne.surf.cloud.api.server.command.ExecutionCommandSource
import dev.slne.surf.cloud.standalone.commands.execution.EntryAction
import dev.slne.surf.cloud.standalone.commands.execution.ExecutionContext
import dev.slne.surf.cloud.standalone.commands.execution.Frame

class FallthroughTask<T : ExecutionCommandSource<out T>> : EntryAction<T> {

    override fun invoke(
        context: ExecutionContext<T>,
        frame: Frame
    ) {
        frame.returnFailure()
        frame.discard()
    }

    companion object {
        private val INSTANCE = FallthroughTask<ExecutionCommandSource<*>>()

        fun <T : ExecutionCommandSource<T>> instance(): EntryAction<T> {
            return INSTANCE as EntryAction<T>
        }
    }
}