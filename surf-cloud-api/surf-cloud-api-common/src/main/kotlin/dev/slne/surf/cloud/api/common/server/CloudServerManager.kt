package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.Unmodifiable


/**
 * Provides management and retrieval operations for cloud servers within the infrastructure.
 *
 * The `CloudServerManager` acts as a centralized interface for interacting with both backend servers
 * (e.g., Paper) and proxy servers (e.g., Velocity). It enables querying servers by their identifiers,
 * categories, or names and supports retrieving lists of servers grouped by category.
 *
 * @see CloudServer
 * @see ProxyCloudServer
 */
@NonExtendable
interface CloudServerManager {

    /**
     * Retrieves a server by its category and name.
     *
     * @param category The category of the server.
     * @param name The name of the server within the specified category.
     * @return The [CommonCloudServer] if found, or `null` if no server matches the provided category and name.
     */
    fun retrieveServerByCategoryAndName(category: String, name: String): CommonCloudServer?

    /**
     * Retrieves a server by its name.
     *
     * @param name The name of the server.
     * @return The [CommonCloudServer] if found, or `null` if no server matches the provided name.
     */
    fun retrieveServerByName(name: String): CommonCloudServer?

    fun retrieveServersInGroup(group: String): ObjectList<out CommonCloudServer>

    @InternalApi
    fun existsServerGroup(name: String): Boolean

    /**
     * Retrieves all servers in a specified category.
     *
     * @param category The category of servers to retrieve.
     * @return An [ObjectList] of [CommonCloudServer] instances belonging to the specified category.
     */
    fun retrieveServersByCategory(category: String): ObjectList<out CommonCloudServer>

    fun retrieveAllServers(): ObjectCollection<out CommonCloudServer>

    fun retrieveServers(): ObjectCollection<out CloudServer>
    fun retrieveProxies(): ObjectCollection<out ProxyCloudServer>

    suspend fun pullPlayersToGroup(
        group: String,
        players: Collection<CloudPlayer>
    ): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>>

    suspend fun broadcastToGroup(
        group: String,
        message: Component,
        permission: String? = null,
        playSound: Boolean = true
    )

    suspend fun broadcast(message: Component, permission: String? = null, playSound: Boolean = true)

    companion object : CloudServerManager by INSTANCE {
        @InternalApi
        val instance = INSTANCE
    }
}

private val INSTANCE = requiredService<CloudServerManager>()