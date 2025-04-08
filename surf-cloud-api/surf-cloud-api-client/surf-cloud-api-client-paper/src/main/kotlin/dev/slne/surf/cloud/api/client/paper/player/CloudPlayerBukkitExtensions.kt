package dev.slne.surf.cloud.api.client.paper.player

import dev.slne.surf.cloud.api.client.paper.toCloudTpCause
import dev.slne.surf.cloud.api.client.paper.toCloudTpFlag
import dev.slne.surf.cloud.api.client.paper.toCloudTpLocation
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.event.player.PlayerTeleportEvent

suspend fun CloudPlayer.teleport(
    location: Location,
    cause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN,
    vararg flags: TeleportFlag
) {
    teleport(
        location.toCloudTpLocation(),
        cause.toCloudTpCause(),
        *flags.map { it.toCloudTpFlag() }.toTypedArray()
    )
}

fun OfflinePlayer.toCloudOfflinePlayer() = CloudPlayerManager.getOfflinePlayer(uniqueId)