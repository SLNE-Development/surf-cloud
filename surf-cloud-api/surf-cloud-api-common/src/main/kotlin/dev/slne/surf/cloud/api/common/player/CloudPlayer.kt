package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.util.*
import kotlin.time.Duration

/**
 * Represents a player connected to the cloud infrastructure.
 *
 * This interface provides access to player metadata, persistent data modification,
 * server connection management, and advanced teleportation utilities. As an [Audience],
 * it enables sending messages or components to the player.
 */
interface CloudPlayer : Audience, OfflineCloudPlayer { // TODO: conversation but done correctly?
    val name: String

    override suspend fun latestIpAddress(): Inet4Address
    override suspend fun lastServerRaw(): String

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

    suspend fun isAfk(): Boolean
    suspend fun currentSessionDuration(): Duration

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

    fun isOnServer(server: CloudServer): Boolean
    fun isInGroup(group: String): Boolean

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

    suspend fun teleport(target: CloudPlayer): Boolean

    override suspend fun displayName(): Component
    override suspend fun name(): String
}

/**
 * Enum representing the result of a player's connection attempt to a server.
 */
enum class ConnectionResultEnum(
    val message: Component,
    val isSuccess: Boolean = false
) {
    SUCCESS(buildText { success("Du hast dich erfolgreich Verbunden.") }, isSuccess = true),
    SERVER_NOT_FOUND(buildText { error("Der Server wurde nicht gefunden.") }),
    SERVER_FULL(buildText { error("Der Server ist voll.") }),
    CATEGORY_FULL(buildText { error("Die Kategorie ist voll.") }),
    SERVER_OFFLINE(buildText { error("Der Server ist offline.") }),
    ALREADY_CONNECTED(buildText { error("Du bist bereits mit diesem Server verbunden.") }),
    CANNOT_SWITCH_PROXY(buildText { error("Du kannst nicht zu diesem Server wechseln, da dieser unter einem anderen Proxy läuft.") }),
    OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET(buildText { error("Der Server kann das Transfer-Paket nicht akzeptieren.") }),
    CANNOT_COMMUNICATE_WITH_PROXY(buildText { error("Der Proxy kann nicht erreicht werden.") }),
    CONNECTION_IN_PROGRESS(buildText { error("Du versucht bereits eine Verbindung zu einem Server herzustellen.") }),
    CONNECTION_CANCELLED(buildText { error("Die Verbindung wurde abgebrochen.") }),
    SERVER_DISCONNECTED(buildText { error("Der Server hat die Verbindung getrennt.") }),
    CANNOT_CONNECT_TO_PROXY(buildText { error("Der Proxy kann nicht erreicht werden.") }),
}

/**
 * Type alias for the result of a connection attempt, comprising a [ConnectionResultEnum] and an optional [Component].
 */
typealias ConnectionResult = Pair<ConnectionResultEnum, Component?>