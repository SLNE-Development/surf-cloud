package dev.slne.surf.cloud.bukkit.command.punish

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.client.paper.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishType
import dev.slne.surf.cloud.bukkit.permission.CloudPermissionRegistry
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.Deferred
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun punishCommand() = commandTree("punish") {
    withPermission(CloudPermissionRegistry.PUNISH_COMMAND)
    offlineCloudPlayerArgument("player") {
        literalArgument("kick") {
            greedyStringArgument("reason") {
                anyExecutor { sender, args ->
                    val player: Deferred<OfflineCloudPlayer?> by args
                    val reason: String by args
                    executeKickCommand(sender, player, reason)
                }
            }
            anyExecutor { sender, args ->
                val player: Deferred<OfflineCloudPlayer?> by args
                executeKickCommand(sender, player)
            }
        }
        literalArgument("mute") {
            greedyStringArgument("reason") {
                anyExecutor { sender, args ->
                    val player: Deferred<OfflineCloudPlayer?> by args
                    val reason: String by args
                    executeMuteCommand(sender, player, reason)
                }
            }
            anyExecutor { sender, args ->
                val player: Deferred<OfflineCloudPlayer?> by args
                executeMuteCommand(sender, player)
            }
        }
        literalArgument("ban") {
            greedyStringArgument("reason") {
                anyExecutor { sender, args ->
                    val player: Deferred<OfflineCloudPlayer?> by args
                    val reason: String by args
                    executeBanCommand(sender, player, reason)
                }
            }
            anyExecutor { sender, args ->
                val player: Deferred<OfflineCloudPlayer?> by args
                executeBanCommand(sender, player)
            }
        }
        literalArgument("warn") {
            greedyStringArgument("reason") {
                anyExecutor { sender, args ->
                    val player: Deferred<OfflineCloudPlayer?> by args
                    val reason: String by args
                    executeWarnCommand(sender, player, reason)
                }
            }
        }
    }
}

private fun executeKickCommand(
    sender: CommandSender,
    player: Deferred<OfflineCloudPlayer?>,
    reason: String? = null
) = plugin.launch {
    val player = player.await() ?: return@launch
    player.punishmentManager.punish(PunishType.KICK, reason, (sender as? Player)?.uniqueId)
    sender.sendText {
        appendPrefix()
        append(player.displayName())
        success(" wurde gekickt.")
    }
}

private fun executeMuteCommand(
    sender: CommandSender,
    player: Deferred<OfflineCloudPlayer?>,
    reason: String? = null
) = plugin.launch {
    val player = player.await() ?: return@launch
    player.punishmentManager.punish(
        PunishType.MUTE.Permanent,
        reason,
        (sender as? Player)?.uniqueId
    )
    sender.sendText {
        appendPrefix()
        append(player.displayName())
        success(" wurde stumm geschaltet.")
    }
}

private fun executeBanCommand(
    sender: CommandSender,
    player: Deferred<OfflineCloudPlayer?>,
    reason: String? = null
) = plugin.launch {
    val player = player.await() ?: return@launch
    if (reason != null) {
        player.punishmentManager.punish(
            PunishType.BAN.Permanent,
            reason,
            (sender as? Player)?.uniqueId
        )
    } else {
        player.punishmentManager.punish(
            PunishType.BAN.Raw,
            null,
            (sender as? Player)?.uniqueId
        )
    }
    sender.sendText {
        appendPrefix()
        append(player.displayName())
        success(" wurde gebannt.")
    }
}

private fun executeWarnCommand(
    sender: CommandSender,
    player: Deferred<OfflineCloudPlayer?>,
    reason: String? = null
) = plugin.launch {
    val player = player.await() ?: return@launch
    player.punishmentManager.punish(
        PunishType.WARN,
        reason,
        (sender as? Player)?.uniqueId
    )
    sender.sendText {
        appendPrefix()
        append(player.displayName())
        success(" wurde verwarnt.")
    }
}