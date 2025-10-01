package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud.OfflineCloudPlayerSerializer
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.player.playtime.Playtime
import dev.slne.surf.cloud.api.common.player.punishment.CloudPlayerPunishmentManager
import dev.slne.surf.cloud.api.common.player.whitelist.CloudPlayerWhitelistManager
import dev.slne.surf.cloud.api.common.server.CloudServer
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*

@Serializable(with = OfflineCloudPlayerSerializer::class)
interface OfflineCloudPlayer {
    /**
     * The unique identifier (UUID) of the player.
     */
    val uuid: UUID

    suspend fun nameHistory(): NameHistory
    suspend fun lastServerRaw(): String?
    suspend fun lastServer(): CloudServer?
    suspend fun lastSeen(): ZonedDateTime?
    suspend fun firstSeen(): ZonedDateTime?
    suspend fun latestIpAddress(): Inet4Address?

    suspend fun playedBefore(): Boolean
    suspend fun playtime(): Playtime

    /**
     * Returns the online player instance if the player is currently connected.
     */
    val player: CloudPlayer?

    val punishmentManager: CloudPlayerPunishmentManager
    val whitelistManager: CloudPlayerWhitelistManager
//    val cache: CloudPlayerCache

    /**
     * Suspends until the display name of the player is retrieved.
     *
     * @return The [Component] representing the player's display name.
     */
    suspend fun displayName(): Component

    suspend fun name(): String?

    /**
     * Retrieves and transforms metadata associated with the player's LuckPerms configuration.
     *
     * @param key The metadata key to retrieve.
     * @param transformer A transformation function applied to the metadata value.
     * @return The transformed value, or `null` if unavailable.
     */
    suspend fun <R> getLuckpermsMetaData(key: String, transformer: (String) -> R): R?

    /**
     * Retrieves metadata associated with the player's LuckPerms configuration.
     *
     * @param key The metadata key to retrieve.
     * @return The value as a string, or `null` if unavailable.
     */
    suspend fun getLuckpermsMetaData(key: String): String?

    companion object {
        operator fun get(uuid: UUID, createIfNotExists: Boolean = true) =
            CloudPlayerManager.getOfflinePlayer(uuid, createIfNotExists)
    }
}