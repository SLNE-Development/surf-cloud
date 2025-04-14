package dev.slne.surf.cloud.bukkit.command.lastseen

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.client.paper.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import kotlinx.coroutines.Deferred
import org.bukkit.command.CommandSender
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

private val dateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    .appendLiteral(" um ")
    .append(DateTimeFormatter.ofPattern("HH:mm:ss"))
    .appendLiteral(" Uhr")
    .toFormatter()
    .withZone(ZoneId.systemDefault())

fun lastSeenCommand() = commandTree("lastseen") {
    withPermission(CloudPermissionRegistry.LAST_SEEN_COMMAND)
    offlineCloudPlayerArgument("player") {
        anyExecutor { sender, args ->
            val player: Deferred<OfflineCloudPlayer?> by args
            sendLastSeen(sender, player)
        }
    }
}

private fun sendLastSeen(sender: CommandSender, player: Deferred<OfflineCloudPlayer?>) = plugin.launch {
    val player = player.await() ?: return@launch
    val lastSeen = player.lastSeen()
    val onlinePlayer = player.player

    when {
        lastSeen == null -> sender.sendNeverSeenMessage(player)
        onlinePlayer?.connected == true -> sender.sendOnlineMessage(player, onlinePlayer)
        else -> sender.sendLastSeenMessage(player, lastSeen)
    }
}

private suspend fun CommandSender.sendNeverSeenMessage(player: OfflineCloudPlayer) = sendText {
    appendPrefix()
    error("Der Spielende ")
    appendPlayerInfo(player)
    error(" wurde noch nie gesehen.")
}

private suspend fun CommandSender.sendOnlineMessage(
    player: OfflineCloudPlayer,
    onlinePlayer: CloudPlayer
) = sendText {
    appendPrefix()
    info("Der Spielende ")
    appendPlayerInfo(player)
    info(" ist seit ")
    variableValue(onlinePlayer.currentSessionDuration().toString())
    info(" auf dem Server ")
    variableValue(onlinePlayer.lastServerRaw())
    info(" online.")
}

private suspend fun CommandSender.sendLastSeenMessage(
    player: OfflineCloudPlayer,
    lastSeen: ZonedDateTime
) = sendText {
    appendPrefix()
    info("Der Spielende ")
    appendPlayerInfo(player)
    info(" wurde zuletzt am ")
    variableValue(dateTimeFormatter.format(lastSeen))
    info(" gesehen.")
}

private suspend fun SurfComponentBuilder.appendPlayerInfo(player: OfflineCloudPlayer) =
    appendAsync {
        variableValue(player.name() ?: player.uuid.toString())
        hoverEvent(buildText { variableValue(player.uuid.toString()) })
    }