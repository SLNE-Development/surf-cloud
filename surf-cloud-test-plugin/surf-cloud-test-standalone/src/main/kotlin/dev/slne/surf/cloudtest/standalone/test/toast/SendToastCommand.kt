package dev.slne.surf.cloudtest.standalone.test.toast

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import dev.slne.surf.cloud.api.common.player.toast.NetworkToast
import dev.slne.surf.cloud.api.server.command.*
import dev.slne.surf.cloud.api.server.command.argument.CloudPlayerArgumentType
import dev.slne.surf.surfapi.core.api.generated.ItemTypeKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.text

@ConsoleCommand
class SendToastCommand : AbstractConsoleCommand() {

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("toast") {
            then("player", CloudPlayerArgumentType.player()) {
                then("title", StringArgumentType.greedyString()) {
                    execute { ctx ->
                        val player = CloudPlayerArgumentType.getPlayer(ctx, "player")
                        val title = StringArgumentType.getString(ctx, "title")

                        player.sendToast {
                            icon(ItemTypeKeys.TRIAL_KEY)
                            title(text(title))
                            frame(NetworkToast.Frame.GOAL)
                        }

                        ctx.source.sendSuccess("Sent toast to player ${player.name}!")
                    }
                }
            }
        })
    }
}