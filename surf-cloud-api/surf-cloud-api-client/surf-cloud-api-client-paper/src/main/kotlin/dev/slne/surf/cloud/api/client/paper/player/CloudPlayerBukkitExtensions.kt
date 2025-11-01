package dev.slne.surf.cloud.api.client.paper.player

import dev.slne.surf.cloud.api.client.paper.toCloudTpCause
import dev.slne.surf.cloud.api.client.paper.toCloudTpFlag
import dev.slne.surf.cloud.api.client.paper.toCloudTpLocation
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Teleports the player to the specified location with the given teleport cause and flags.
 *
 * @param location The location to teleport the player to.
 * @param cause The reason for the teleportation, typically a specific event or trigger. Defaults to [PlayerTeleportEvent.TeleportCause.PLUGIN].
 * @param flags Additional flags that define special conditions or behaviors for the teleportation.
 */
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

/**
 * Converts an `OfflinePlayer` instance to a `OfflineCloudPlayer` instance
 * by retrieving the corresponding player data from the `CloudPlayerManager` using the player's UUID.
 *
 * This function ensures that the `OfflineCloudPlayer` exists in the underlying system,
 * creating a placeholder entry if necessary.
 *
 * @return An `OfflineCloudPlayer` representing the player associated with the given `OfflinePlayer`.
 */
fun OfflinePlayer.toCloudOfflinePlayer() = CloudPlayerManager.getOfflinePlayer(uniqueId)

/**
 * Converts the current [CloudPlayer] instance into a Bukkit player instance.
 *
 * This method retrieves the corresponding Bukkit [Player] object based on the UUID of the
 * current [CloudPlayer]. If the player is not currently connected to the server, this will
 * return `null`.
 *
 * @return The Bukkit [Player] instance corresponding to the [CloudPlayer]'s UUID, or `null`
 *         if the player is not present on the Bukkit server.
 */
fun CloudPlayer.toBukkitPlayer() = server.getPlayer(uuid)

/**
 * Converts an instance of [OfflineCloudPlayer] to the corresponding Bukkit offline player.
 *
 * This function uses the player's unique identifier (UUID) to fetch the associated
 * [org.bukkit.OfflinePlayer] from the server.
 *
 * @receiver The [OfflineCloudPlayer] instance to convert.
 * @return The corresponding [org.bukkit.OfflinePlayer] for the specified [OfflineCloudPlayer].
 */
fun OfflineCloudPlayer.toBukkitOfflinePlayer() = server.getOfflinePlayer(uuid)