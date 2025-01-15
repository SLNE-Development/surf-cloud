package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.util.requiredService
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Manages online players within the cloud infrastructure.
 *
 * This interface provides methods for retrieving player information by their UUID
 * and integrates with the [CloudPlayer] system.
 */
@ApiStatus.NonExtendable
interface CloudPlayerManager {

    /**
     * Retrieves a player by their UUID.
     *
     * @param uuid The UUID of the player to retrieve. If `null`, the operation will return `null`.
     * @return The [CloudPlayer] instance if the player is online, or `null` otherwise.
     */
    fun getPlayer(uuid: UUID?): CloudPlayer?

    companion object {
        val instance = requiredService<CloudPlayerManager>()
    }
}

val playerManager get() = CloudPlayerManager.instance

/**
 * Attempts to convert an [Audience] to its corresponding [CloudPlayer].
 *
 * @receiver The [Audience] to convert.
 * @return The [CloudPlayer] if the [Audience] represents a player,
 * or `null` if the [Audience] is not a player or cannot be resolved.
 */
fun Audience?.toCloudPlayer(): CloudPlayer? {
    return playerManager.getPlayer(this?.pointers()?.get(Identity.UUID)?.orElse(null))
}