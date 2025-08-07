package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistSettings
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.server.command.*
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component

@ConsoleCommand
class WhitelistCommand : AbstractConsoleCommand() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("whitelist") {
            then("status") {
                executes { ctx ->
                    sendStatus(ctx.source)
                    Command.SINGLE_SUCCESS
                }
            }
            then("reload") {
                executes { ctx ->
                    reloadConfig(ctx.source)
                    Command.SINGLE_SUCCESS
                }
            }
            then("refresh") {
                executes { ctx ->
                    refreshWhitelist(ctx.source)
                    Command.SINGLE_SUCCESS
                }
            }
        })
    }

    private fun sendStatus(source: CommandSource) {
        val groupedServer = CloudServer.all().groupBy { it.group }

        fun SurfComponentBuilder.appendWhitelistStatus(enabled: Boolean, inherited: Boolean) {
            spacer("[")
            if (enabled) {
                text("✅", Colors.WHITE)
            } else if (inherited) {
                text("🔗", Colors.WHITE)
            } else {
                text("❌", Colors.WHITE)
            }
            spacer("]")
        }

        source.sendInfo(buildText {
            spacer("——————————————————————— ")
            primary("Whitelist")
            spacer(" ——————————————————————————")

            for ((group, server) in groupedServer) {
                val enforcedForGroup = WhitelistSettings.isWhitelistEnforcedForGroup(group)

                spacer("  — ")
                variableKey("Group: ")
                variableValue(group)
                appendSpace()
                appendWhitelistStatus(enforcedForGroup, false)

                appendCollectionNewLine(server, Component.text("    — ", Colors.SPACER)) { server ->
                    buildText {
                        variableValue(server.name)
                        appendSpace()
                        appendWhitelistStatus(
                            WhitelistSettings.isWhitelistEnforcedForServer(server.name),
                            enforcedForGroup
                        )
                    }
                }
            }

            spacer("————————————————————————————————————————————————————————————")
        })
    }

    private fun reloadConfig(source: CommandSource) {
        source.sendFailure("Reloading whitelist is not implemented yet.")
    }

    private fun refreshWhitelist(source: CommandSource) {
        source.sendInfo("Refreshing whitelist enforcement...")
        WhitelistSettings.refresh()
    }
}