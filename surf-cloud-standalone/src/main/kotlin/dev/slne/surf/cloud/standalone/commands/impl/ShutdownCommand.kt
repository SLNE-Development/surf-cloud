package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal
import kotlin.system.exitProcess

object ShutdownCommand: ConsoleCommand {
    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        repeat(20) {
            println("This is a fake loop")
        }
        dispatcher.register(literal("stop") {
            executes { context ->
                context.source.sendSuccess("Stopping standalone server...")
                exitProcess(ExitCodes.NORMAL)
            }
        })
    }
}