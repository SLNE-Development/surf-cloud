package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal

object TestCommand : ConsoleCommand {
    fun register(dispatcher: CommandDispatcher<CommandSource>) {
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