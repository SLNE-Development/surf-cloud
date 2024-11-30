package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.state.ServerState
import net.kyori.adventure.audience.ForwardingAudience
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a server within the cloud infrastructure.
 *
 * Provides metadata about the server, access to its player list, and operations
 * for managing players and server behavior.
 */
@ApiStatus.NonExtendable
interface CommonCloudServer : ForwardingAudience {

    /**
     * The unique identifier (UID) of the server.
     */
    val uid: Long

    /**
     * The group this server belongs to.
     */
    val group: String

    /**
     * The name of the server.
     */
    val name: String

    /**
     * The maximum number of players allowed on the server.
     */
    val maxPlayerCount: Int

    /**
     * The current number of players on the server.
     */
    val currentPlayerCount: Int

    /**
     * The number of empty slots available on the server.
     */
    val emptySlots: Int
        get() = maxPlayerCount - currentPlayerCount

    /**
     * The current state of the server.
     */
    val state: ServerState

    /**
     * The list of users currently on the server.
     */
    val users: UserList

    /**
     * Provides the list of audiences corresponding to the server's users.
     *
     * @return A [UserList] representing the players on the server.
     */
    override fun audiences(): UserList

    /**
     * Sends all players on this server to the specified target server.
     *
     * This operation suspends until all players have been transferred.
     *
     * @param server The target server to send the players to.
     */
    suspend fun sendAll(server: CommonCloudServer)

    /**
     * Sends players matching the specified filter on this server to the target server.
     *
     * This operation suspends until all matching players have been transferred.
     *
     * @param server The target server to send the players to.
     * @param filter A filter function to determine which players should be sent.
     */
    suspend fun sendAll(server: CommonCloudServer, filter: (CloudPlayer) -> Boolean)

    /**
     * Shuts down the server.
     *
     * This will terminate all processes associated with the server.
     */
    fun shutdown()
}
