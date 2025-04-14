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
     * Retrieves a server by its unique ID.
     *
     * @param id The unique identifier of the server.
     * @return The [CommonCloudServer] if found, or `null` if no server matches the provided ID.
     */
    suspend fun retrieveServerById(id: Long): CommonCloudServer?

    /**
     * Retrieves a server by its category and name.
     * If multiple servers share the same name within the same category,
     * the server with the lowest player count will be returned.
     *
     * @param category The category of the server.
     * @param name The name of the server within the specified category.
     * @return The [CommonCloudServer] if found, or `null` if no server matches the provided category and name.
     */
    suspend fun retrieveServerByCategoryAndName(category: String, name: String): CommonCloudServer?

    /**
     * Retrieves a server by its name. If multiple servers share the same name,
     * the server with the lowest player count will be returned.
     *
     * @param name The name of the server.
     * @return The [CommonCloudServer] if found, or `null` if no server matches the provided name.
     */
    suspend fun retrieveServerByName(name: String): CommonCloudServer?

    suspend fun retrieveServersInGroup(group: String): ObjectList<out CommonCloudServer>

    @InternalApi
    fun getServerByNameUnsafe(name: String): CloudServer?

    @InternalApi
    fun existsServerGroup(name: String): Boolean

    /**
     * Retrieves all servers in a specified category.
     *
     * @param category The category of servers to retrieve.
     * @return An [ObjectList] of [CommonCloudServer] instances belonging to the specified category.
     */
    suspend fun retrieveServersByCategory(category: String): ObjectList<out CommonCloudServer>

    suspend fun retrieveAllServers(): ObjectCollection<out CommonCloudServer>

    suspend fun pullPlayersToGroup(
        group: String,
        players: Collection<CloudPlayer>
    ): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>>

    suspend fun broadcastToGroup(group: String, message: Component)
    suspend fun broadcast(message: Component)

    companion object : CloudServerManager by INSTANCE {
        @InternalApi
        val instance = INSTANCE
    }
}

private val INSTANCE = requiredService<CloudServerManager>()