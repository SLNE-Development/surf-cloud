package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.server.CloudServer
import net.kyori.adventure.text.Component
import java.net.InetAddress
import java.time.ZonedDateTime
import java.util.*

interface OfflineCloudPlayer {
    /**
     * The unique identifier (UUID) of the player.
     */
    val uuid: UUID

    suspend fun nameHistory(): NameHistory
    suspend fun lastServerRaw(): String?
    suspend fun lastServer(): CloudServer?
    suspend fun lastSeen(): ZonedDateTime?
    suspend fun latestIpAddress(): InetAddress?

    suspend fun playedBefore(): Boolean

    /**
     * Returns the online player instance if the player is currently connected.
     */
    val player: CloudPlayer?

    /**
     * Suspends until the display name of the player is retrieved.
     *
     * @return The [Component] representing the player's display name.
     */
    suspend fun displayName(): Component?

    /**
     * Retrieves metadata associated with the player's LuckPerms configuration.
     *
     * @param key The metadata key to retrieve.
     * @return The value as a string, or `null` if unavailable.
     */
    suspend fun <R> getLuckpermsMetaData(key: String, transformer: (String) -> R): R?

    /**
     * Retrieves and transforms metadata associated with the player's LuckPerms configuration.
     *
     * @param key The metadata key to retrieve.
     * @param transformer A transformation function applied to the metadata value.
     * @return The transformed value, or `null` if unavailable.
     */
    suspend fun getLuckpermsMetaData(key: String): String?

}