package dev.slne.surf.cloud.bukkit.command.connection

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.client.paper.command.args.onlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.surfapi.bukkit.api.command.args.MiniMessageArgument
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

fun disconnectPlayerCommand() = commandTree("disconnect") {
    onlineCloudPlayerArgument("player") {
        anyExecutor { sender, args ->
            val player: CloudPlayer by args
            disconnect(sender, player)
        }

        argument(MiniMessageArgument("reason")) {
            anyExecutor { sender, args ->
                val player: CloudPlayer by args
                val reason: Component by args
                disconnect(sender, player, reason)
            }
        }
    }
}

private fun disconnect(sender: CommandSender, player: CloudPlayer, reason: Component? = null) {
    val reason = reason ?: buildText {
        appendDisconnectHeader()
        error("DU WURDEST VOM NETZWERK GEWORFEN")
        appendNewline(3)
        append(CommonComponents.RETRY_LATER_FOOTER)
    }

    player.disconnect(reason)
    sender.sendText {
        appendPrefix()
        success("Der Spielende ")
        variableValue(player.name)
        success(" wurde erfolgreich vom Netzwerk getrennt.")
    }
}