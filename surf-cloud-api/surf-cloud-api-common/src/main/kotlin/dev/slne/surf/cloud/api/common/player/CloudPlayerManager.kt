package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.util.requiredService
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface CloudPlayerManager {

    /**
     * Retrieves a player by their UUID. Returns `null` if the player is not online or if the provided UUID is `null`.
     *
     * @param uuid The UUID of the player to retrieve.
     * @return The [CloudPlayer] instance if the player is online, or `null` otherwise.
     */
    fun getPlayer(uuid: UUID?): CloudPlayer?

    companion object {
        /**
         * The singleton instance of the [CloudPlayerManager].
         */
        val instance = requiredService<CloudPlayerManager>()
    }
}

/**
 * A global reference to the singleton [CloudPlayerManager] instance.
 */
val playerManager get() = CloudPlayerManager.instance

/**
 * Converts an [Audience] to its corresponding [CloudPlayer].
 *
 * @return The [CloudPlayer] if the [Audience] is a player,
 * or `null` if the [Audience] is not a player or cannot be resolved.
 */
fun Audience?.toCloudPlayer(): CloudPlayer? {
    return playerManager.getPlayer(this?.pointers()?.get(Identity.UUID)?.orElse(null))
}