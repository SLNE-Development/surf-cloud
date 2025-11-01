package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.server.command.*
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder

@ConsoleCommand
class ConfigCommand(private val configHolder: StandaloneConfigHolder) : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("config") {
            then("reload") {
                executes { ctx ->
                    configHolder.reloadFromFile()
                    ctx.source.sendSuccess("Config reloaded from file.")
                    Command.SINGLE_SUCCESS
                }
            }

            then("save") {
                executes { ctx ->
                    configHolder.saveToFile()
                    ctx.source.sendSuccess("Config saved to file.")
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }
}