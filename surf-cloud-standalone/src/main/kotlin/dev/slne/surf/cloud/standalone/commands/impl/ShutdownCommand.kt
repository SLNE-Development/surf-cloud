package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal
import dev.slne.surf.cloud.core.common.coroutines.ServerShutdownScope
import dev.slne.surf.cloud.standalone.Bootstrap
import kotlinx.coroutines.launch

@ConsoleCommand
class ShutdownCommand : AbstractConsoleCommand() {

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("stop") {
            executes { context ->
                context.source.sendSuccess("Stopping standalone server...")
                ServerShutdownScope.launch { Bootstrap.shutdown(ExitCodes.NORMAL) }
                1
            }
        })
    }
}