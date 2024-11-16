package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.util.requiredService
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface CloudPlayerManager {

    /**
     * Get a player by their UUID. This will return null if the player is not online.
     * @param uuid The UUID of the player to get.
     * @return The player, or null if they are not online or the provided UUID is null.
     */
    fun getPlayer(uuid: UUID?): CloudPlayer?

    companion object {
        val instance = requiredService<CloudPlayerManager>()
    }
}

val playerManager get() = CloudPlayerManager.instance

/**
 * Get the CloudPlayer for this Audience.
 * @return The CloudPlayer, or null if the Audience is not a player.
 */
fun Audience.toCloudPlayer(): CloudPlayer? {
    return playerManager.getPlayer(this.pointers().get(Identity.UUID).orElse(null))
}