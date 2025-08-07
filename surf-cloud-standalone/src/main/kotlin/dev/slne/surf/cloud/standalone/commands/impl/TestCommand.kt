package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand

@ConsoleCommand
class TestCommand : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
//        dispatcher.register(literal("test") {
//            executes { context ->
//                context.source.sendSuccess("This is a test command")
//                1
//            }
//        })

        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource>("test")
            .executes { context ->
                context.source.sendSuccess("This is a test command")
                1
            })
    }
}