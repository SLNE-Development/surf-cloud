package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration

@ConsoleCommand
class HelpCommand : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("help") {
            executes { ctx ->
                val smartUsage = dispatcher.getSmartUsage(dispatcher.root, ctx.source)

                val message = buildText {
                    info("Available commands:")
                    appendNewline()
                    append(
                        Component.join(
                            JoinConfiguration.newlines(),
                            smartUsage.values.map { usage ->
                                buildText {
                                    val commandParts = usage.split(" ")
                                    val commandKey = commandParts[0]
                                    val arguments = commandParts.drop(1).joinToString(" ")

                                    variableKey(commandKey)
                                    appendSpace()
                                    spacer(arguments)
                                }
                            }
                        )
                    )
                }

                ctx.source.sendMessage(message)

                Command.SINGLE_SUCCESS
            }
        })
    }
}