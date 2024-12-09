package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.server.CloudServer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Represents a player connected to the cloud infrastructure.
 *
 * Provides methods to access player-related metadata, manage server connections,
 * and modify persistent data containers. This interface also extends [Audience],
 * allowing interactions with the player via messages and components.
 */
interface CloudPlayer : Audience {
    /**
     * The unique identifier (UUID) of the player.
     */
    val uuid: UUID

    /**
     * A read-only view of the player's persistent data container.
     *
     * This view provides access to the player's metadata, enabling operations such as:
     * - Checking for specific metadata keys.
     * - Retrieving values associated with metadata keys.
     * - Listing all metadata keys present in the container.
     */
    val persistentDataView: PersistentPlayerDataContainerView

    /**
     * Indicates whether the player is currently connected to a proxy server.
     *
     * This property is `true` if the player is connected to a proxy, and `false` otherwise.
     */
    val connectedToProxy: Boolean

    /**
     * Indicates whether the player is currently connected to a real server (e.g., Paper server).
     *
     * This property is `true` if the player is connected to a server, and `false` otherwise.
     */
    val connectedToServer: Boolean

    /**
     * Indicates whether the player is connected to either a proxy or a server.
     *
     * This property combines [connectedToProxy] and [connectedToServer],
     * returning `true` if the player is connected to either of them, and `false` otherwise.
     */
    val connected get() = connectedToProxy || connectedToServer

    /**
     * Retrieves the display name of the player.
     *
     * This method suspends until the display name is fully retrieved.
     *
     * @return The [Component] representing the player's display name.
     */
    suspend fun displayName(): Component

    /**
     * Edits the player's persistent data container asynchronously.
     *
     * This method allows modifications to the persistent data associated with the player.
     * The operation suspends until the changes are fully applied
     * and synchronized across the network.
     *
     * @param block A suspending lambda function defining the modifications to the data container.
     */
    suspend fun editPersistentData(block: suspend PersistentPlayerDataContainer.() -> Unit)

    /**
     * Connects the player to a specified server.
     *
     * Attempts to connect the player to the provided
     * [server] and suspends until the operation completes.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(server: CloudServer): ConnectionResult

    /**
     * Connects the player to a specified server by group and name.
     *
     * Attempts to connect the player to the server identified by the [group] and [server] name.
     * Suspends until the operation completes.
     *
     * @param group The server group.
     * @param server The name of the target server.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in a specified group.
     *
     * Suspends until the connection attempt completes.
     *
     * @param group The server group to connect to.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String): ConnectionResult

    /**
     * Connects the player to a specified server or queues the player if the server is unavailable.
     *
     * If the server is offline or full,
     * the player will be placed in a queue to connect when the server becomes available.
     * This method suspends until the player is fully connected to the server.
     * It does not complete while the player remains in the queue.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResult

    /**
     * Connects the player to a specified server by group and name or queues the player if the server is unavailable.
     *
     * If the server is offline or full, the player will be placed in a queue to connect when the server becomes available.
     * This method suspends until the player is fully connected to the server. It does not complete while the player remains in the queue.
     *
     * @param group The server group.
     * @param server The name of the target server.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in a specified group
     * or queues the player if unavailable.
     *
     * If no server in the group is available,
     * the player will be placed in a queue to connect when a server becomes available.
     * This method suspends until the player is fully connected to a server.
     * It does not complete while the player remains in the queue.
     *
     * @param group The server group to connect to.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(group: String): ConnectionResult

    suspend fun <R> getLuckpermsMetaData(key: String, transformer: (String) -> R): R?
    suspend fun getLuckpermsMetaData(key: String): String?
}

/**
 * Represents the result of a connection attempt to a server.
 */
enum class ConnectionResultEnum {
    /**
     * Indicates that the connection to the server was successful.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SUCCESS,

    /**
     * Indicates that the specified server was not found.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SERVER_NOT_FOUND,

    /**
     * Indicates that the specified server is full and cannot accept new connections.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SERVER_FULL,

    CATEGORY_FULL,

    /**
     * Indicates that the specified server is offline and unavailable for connections.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SERVER_OFFLINE,

    /**
     * Indicates that the player is already connected to the specified server.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    ALREADY_CONNECTED,

    CANNOT_SWITCH_PROXY,

    OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET,

    CANNOT_COMMUNICATE_WITH_PROXY,
    CONNECTION_IN_PROGRESS,
    CONNECTION_CANCELLED,
    SERVER_DISCONNECTED,

    CANNOT_CONNECT_TO_PROXY,

}

typealias ConnectionResult = Pair<ConnectionResultEnum, Component?>