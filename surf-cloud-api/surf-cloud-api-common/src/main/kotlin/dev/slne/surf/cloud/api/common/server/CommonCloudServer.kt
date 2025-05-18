package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.state.ServerState
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus

/**
 * Represents the result of a batch transfer operation.
 *
 * The result is a pair of a boolean indicating the overall success of the operation,
 * and a map of [CloudPlayer]s to [ConnectionResultEnum]s indicating the result of each transfer.
 */
typealias BatchTransferResult = Pair<Boolean, Object2ObjectMap<CloudPlayer, ConnectionResultEnum>>

/**
 * Represents a server within the cloud infrastructure.
 *
 * This interface provides access to metadata about the server, its player list,
 * and methods for managing player transfers and server operations.
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
     * Checks if the server has available slots for new players.
     *
     * @return `true` if the server has empty slots, otherwise `false`.
     */
    fun hasEmptySlots(): Boolean = emptySlots > 0

    /**
     * The current state of the server.
     */
    val state: ServerState

    /**
     * The list of players currently on the server.
     */
    val users: UserList

    val displayName: String
        get() = "$group/$uid $name"

    /**
     * Provides the list of audiences corresponding to the server's users.
     *
     * @return A [UserList] representing the players on the server.
     */
    override fun audiences(): UserList

    /**
     * Sends all players on this server to the specified target server.
     *
     * This operation suspends until all players have been successfully transferred.
     *
     * @param server The target server to transfer players to.
     * @return A [BatchTransferResult] indicating the result of the transfer operation.
     */
    suspend fun sendAll(server: CloudServer): BatchTransferResult

    /**
     * Sends a subset of players, filtered by a condition, to the specified target server.
     *
     * This operation suspends until all matching players have been successfully transferred.
     *
     * @param server The target server to transfer players to.
     * @param filter A filter function to determine which players to transfer.
     * @return A [BatchTransferResult] indicating the result of the transfer operation.
     */
    suspend fun sendAll(server: CloudServer, filter: (CloudPlayer) -> Boolean): BatchTransferResult

    suspend fun sendAll(category: String): BatchTransferResult

    fun isInGroup(group: String): Boolean

    suspend fun broadcast(message: Component, permission: String? = null, playSound: Boolean = true)

    /**
     * Shuts down the server.
     *
     * This terminates all processes and disconnects all players.
     */
    fun shutdown()
}
