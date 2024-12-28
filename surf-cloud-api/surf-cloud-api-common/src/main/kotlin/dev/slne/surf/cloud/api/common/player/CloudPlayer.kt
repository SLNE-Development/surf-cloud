package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.position.FineLocation
import dev.slne.surf.cloud.api.common.util.position.FineTeleportCause
import dev.slne.surf.cloud.api.common.util.position.FineTeleportFlag
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
interface CloudPlayer : Audience { // TODO: conversation but done correctly?, teleport api
    /**
     * The unique identifier (UUID) of the player.
     */
    val uuid: UUID

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
    suspend fun <R> withPersistentData(block: PersistentPlayerDataContainer.() -> R): R

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

    /**
     * Disconnects the player from the network.
     *
     * @param reason The reason for the disconnection.
     */
    fun disconnect(reason: Component)

    /**
     * Teleports the player to a specified location.
     *
     * This method suspends until the player is teleported to the target location.
     *
     * @param location The target location to teleport the player to.
     * @param teleportCause The cause of the teleportation.
     * @param flags An array of [FineTeleportFlag] to apply during the teleportation.
     *
     * @throws IllegalArgumentException if the player is teleported to a location in a different world.
     * @throws IllegalStateException if the player is not connected to a bukkit server and thus cannot be teleported.
     */
    suspend fun teleport(
        location: FineLocation,
        teleportCause: FineTeleportCause = FineTeleportCause.PLUGIN,
        vararg flags: FineTeleportFlag
    ): Boolean

    /**
     * Teleports the player to a specified location.
     *
     * This method suspends until the player is teleported to the target location.
     * Proxies everything to [teleport].
     *
     * @param world The UUID of the world to teleport the player to.
     * @param x The x-coordinate of the target location.
     * @param y The y-coordinate of the target location.
     * @param z The z-coordinate of the target location.
     * @param yaw The yaw rotation of the player.
     * @param pitch The pitch rotation of the player.
     * @param teleportCause The cause of the teleportation.
     * @param flags An array of [FineTeleportFlag] to apply during the teleportation.
     *
     * @throws IllegalArgumentException if the player is teleported to a location in a different world.
     * @throws IllegalStateException if the player is not connected to a bukkit server and thus cannot be teleported.
     *
     * @see teleport(FineLocation)
     */
    suspend fun teleport(
        world: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float = 0.0f,
        pitch: Float = 0.0f,
        teleportCause: FineTeleportCause = FineTeleportCause.PLUGIN,
        vararg flags: FineTeleportFlag,
    ) = teleport(FineLocation(world, x, y, z, yaw, pitch), teleportCause, *flags)

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