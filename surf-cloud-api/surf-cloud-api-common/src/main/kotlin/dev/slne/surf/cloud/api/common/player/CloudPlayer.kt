package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.server.CloudServer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Represents a player connected to the cloud infrastructure.
 *
 * This interface provides access to player metadata, persistent data modification,
 * server connection management, and advanced teleportation utilities. As an [Audience],
 * it enables sending messages or components to the player.
 */
interface CloudPlayer : Audience, OfflineCloudPlayer { // TODO: conversation but done correctly?
    /**
     * Whether the player is currently connected to a proxy server.
     */
    val connectedToProxy: Boolean
    /**
     * Whether the player is currently connected to a real server.
     */
    val connectedToServer: Boolean

    /**
     * Whether the player is connected to either a proxy or a server.
     */
    val connected get() = connectedToProxy || connectedToServer

    /**
     * Performs modifications on the player's persistent data container.
     *
     * @param block A suspending block to modify the persistent data container.
     * @return The result of the block execution.
     */
    suspend fun <R> withPersistentData(block: PersistentPlayerDataContainer.() -> R): R

    /**
     * Connects the player to a specified server.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(server: CloudServer): ConnectionResult

    /**
     * Connects the player to a server by its group and name.
     *
     * @param group The server group name.
     * @param server The target server name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in the specified group.
     *
     * @param group The server group name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String): ConnectionResult

    /**
     * Connects the player to the specified server or places them in a queue if unavailable.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResult

    /**
     * Connects the player to a server by group and name or places them in a queue if unavailable.
     *
     * @param group The server group name.
     * @param server The target server name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in a group or queues them if unavailable.
     *
     * @param group The server group name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(group: String): ConnectionResult

    /**
     * Disconnects the player from the network with a specified reason.
     *
     * @param reason The reason for the disconnection.
     */
    fun disconnect(reason: Component)

    /**
     * Teleports the player to a specified location.
     *
     * @param location The target location for teleportation.
     * @param teleportCause The reason for teleportation.
     * @param flags Additional flags for teleportation.
     * @return `true` if the teleportation was successful, `false` otherwise.
     *
     * @throws IllegalArgumentException If teleporting to a different world is invalid.
     * @throws IllegalStateException If the player is not connected to a supported server.
     */
    suspend fun teleport(
        location: TeleportLocation,
        teleportCause: TeleportCause = TeleportCause.PLUGIN,
        vararg flags: TeleportFlag
    ): Boolean

    /**
     * Teleports the player to specific coordinates.
     *
     * @param world The UUID of the world.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @param yaw The yaw angle.
     * @param pitch The pitch angle.
     * @param teleportCause The reason for teleportation.
     * @param flags Additional flags for teleportation.
     * @return Delegates to the other [teleport] method.
     */
    suspend fun teleport(
        world: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float = 0.0f,
        pitch: Float = 0.0f,
        teleportCause: TeleportCause = TeleportCause.PLUGIN,
        vararg flags: TeleportFlag,
    ) = teleport(TeleportLocation(world, x, y, z, yaw, pitch), teleportCause, *flags)

    override suspend fun displayName(): Component
}

/**
 * Enum representing the result of a player's connection attempt to a server.
 */
enum class ConnectionResultEnum {
    SUCCESS,
    SERVER_NOT_FOUND,
    SERVER_FULL,
    CATEGORY_FULL,
    SERVER_OFFLINE,
    ALREADY_CONNECTED,
    CANNOT_SWITCH_PROXY,
    OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET,
    CANNOT_COMMUNICATE_WITH_PROXY,
    CONNECTION_IN_PROGRESS,
    CONNECTION_CANCELLED,
    SERVER_DISCONNECTED,
    CANNOT_CONNECT_TO_PROXY,
}

/**
 * Type alias for the result of a connection attempt, comprising a [ConnectionResultEnum] and an optional [Component].
 */
typealias ConnectionResult = Pair<ConnectionResultEnum, Component?>