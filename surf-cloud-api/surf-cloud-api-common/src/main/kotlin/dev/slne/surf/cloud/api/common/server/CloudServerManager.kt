package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.ApiStatus.NonExtendable

/**
 * Manages interactions with cloud servers within the infrastructure.
 *
 * Provides methods for retrieving servers by various identifiers or categories.
 */
@NonExtendable
interface CloudServerManager {

    /**
     * Retrieves a server by its unique ID.
     *
     * @param id The unique identifier of the server.
     * @return The [CloudServer] if found, or `null` if no server matches the provided ID.
     */
    suspend fun retrieveServerById(id: Long): CloudServer?

    /**
     * Retrieves a server by its category and name.
     *
     * @param category The category of the server.
     * @param name The name of the server within the specified category.
     * @return The [CloudServer] if found, or `null` if no server matches the provided category and name.
     */
    suspend fun retrieveServerByCategoryAndName(category: String, name: String): CloudServer?

    /**
     * Retrieves a server by its name.
     *
     * @param name The name of the server.
     * @return The [CloudServer] if found, or `null` if no server matches the provided name.
     */
    suspend fun retrieveServerByName(name: String): CloudServer?

    /**
     * Retrieves all servers in a specified category.
     *
     * @param category The category of servers to retrieve.
     * @return An [ObjectList] of [CloudServer] instances belonging to the specified category.
     */
    suspend fun retrieveServersByCategory(category: String): ObjectList<out CloudServer>

    companion object {
        /**
         * The singleton instance of the [CloudServerManager].
         */
        val instance = requiredService<CloudServerManager>()
    }
}

/**
 * A global reference to the singleton [CloudServerManager] instance.
 */
val serverManager get() = CloudServerManager.instance