package dev.slne.surf.cloudtest.standalone.test.command

import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.server.command.*
import dev.slne.surf.cloud.api.server.command.argument.CloudPlayerArgumentType
import dev.slne.surf.cloudtest.core.test.ppdc.PpdcTestExecutor
import dev.slne.surf.cloudtest.standalone.test.plugin

@ConsoleCommand
class TestPpdcCommand(private val ppdcTestExecutor: PpdcTestExecutor) : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("test-ppdc") {
            then("player", CloudPlayerArgumentType.player()) {
                then("set") {
                    execute { ctx ->
                        val player = CloudPlayerArgumentType.getPlayer(ctx, "player")
                        plugin.launch {
                            ppdcTestExecutor.setRandomPpdcTestData(ctx.source, player)
                        }
                    }
                }

                then("get") {
                    execute { ctx ->
                        val player = CloudPlayerArgumentType.getPlayer(ctx, "player")
                        plugin.launch {
                            ppdcTestExecutor.showPpdcTestData(ctx.source, player)
                        }
                    }
                }
            }
        })
    }
}