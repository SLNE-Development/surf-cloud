package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.server.command.*
import dev.slne.surf.cloud.api.server.command.argument.CloudGroupArgumentType
import dev.slne.surf.cloud.api.server.command.argument.CloudServerArgumentType
import dev.slne.surf.cloud.core.common.command.GlistCommandExecutor

@ConsoleCommand
class ListCommand : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("list") {
            execute { ctx ->
                GlistCommandExecutor.displayPlayerCount(ctx.source)
            }

            then("all") {
                execute { ctx ->
                    GlistCommandExecutor.displayAllOnlinePlayers(ctx.source)
                }
            }

            then("group") {
                then("group", CloudGroupArgumentType.group()) {
                    execute { ctx ->
                        val group = CloudGroupArgumentType.getGroup(ctx, "group")
                        GlistCommandExecutor.displayOnlinePlayersInGroup(ctx.source, group)
                    }
                }
            }


            then("server") {
                then("server", CloudServerArgumentType.server()) {
                    execute { ctx ->
                        val server = CloudServerArgumentType.getServer(ctx, "server")
                        GlistCommandExecutor.displayOnlinePlayersOnServer(ctx.source, server)
                    }
                }
            }
        })
    }
}