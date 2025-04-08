package dev.slne.surf.cloud.standalone.commands.execution.tasks

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ContextChain
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.slne.surf.cloud.api.server.command.ExecutionCommandSource
import dev.slne.surf.cloud.standalone.commands.execution.ChainModifiers
import dev.slne.surf.cloud.standalone.commands.execution.ExecutionContext
import dev.slne.surf.cloud.standalone.commands.execution.Frame
import dev.slne.surf.cloud.standalone.commands.execution.UnboundEntryAction

class ExecuteCommand<T : ExecutionCommandSource<T>>(
    private val modifiers: ChainModifiers,
    private val executionContext: CommandContext<T>
) : UnboundEntryAction<T> {
    override fun execute(
        source: T,
        executionContext: ExecutionContext<T>,
        frame: Frame
    ) {
        try {
            executionContext.incrementCost()
            ContextChain.runExecutable(
                this.executionContext,
                source,
                ExecutionCommandSource.resultConsumer(),
                modifiers.isForked()
            )
        } catch (e: CommandSyntaxException) {
            source.handleError(e, modifiers.isForked())
        }
    }
}