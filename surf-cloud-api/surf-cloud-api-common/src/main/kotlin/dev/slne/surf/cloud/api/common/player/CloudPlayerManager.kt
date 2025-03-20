package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.util.*
import kotlin.jvm.optionals.getOrNull

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

    fun getOfflinePlayer(uuid: UUID): OfflineCloudPlayer

    companion object : CloudPlayerManager by INSTANCE {
        @InternalApi
        val instance = INSTANCE
    }
}

private val INSTANCE = requiredService<CloudPlayerManager>()

/**
 * Attempts to convert an [Audience] to its corresponding [CloudPlayer].
 *
 * @receiver The [Audience] to convert.
 * @return The [CloudPlayer] if the [Audience] represents a player,
 * or `null` if the [Audience] is not a player or cannot be resolved.
 */
fun Audience?.toCloudPlayer(): CloudPlayer? {
    return CloudPlayerManager.getPlayer(this?.pointers()?.get(Identity.UUID)?.getOrNull())
}

/**
 * Attempts to convert an [Audience] to its corresponding [OfflineCloudPlayer].
 *
 * @receiver The [Audience] to convert.
 * @return The [OfflineCloudPlayer] if the [Audience] represents a player,
 * or `null` if the [Audience] is not a player or cannot be resolved.
 */
fun Audience?.toOfflineCloudPlayer(): OfflineCloudPlayer? {
    return this?.pointers()?.get(Identity.UUID)?.getOrNull()?.let { CloudPlayerManager.getOfflinePlayer(it) }
}