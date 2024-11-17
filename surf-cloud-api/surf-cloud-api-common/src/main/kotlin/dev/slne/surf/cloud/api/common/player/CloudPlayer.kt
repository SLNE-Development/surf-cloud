package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.server.CloudServer
import net.kyori.adventure.audience.Audience
import java.util.*

interface CloudPlayer: Audience {
    /**
     * The UUID of the player.
     */
    val uuid: UUID

    /**
     * Represents a view of the persistent data container for the player.
     *
     * Provides a read-only interface to access the persistent data associated with the player.
     * This includes checking for the presence of metadata, retrieving stored values,
     * and listing the keys present in the data container.
     */
    val persistentDataView: PersistentPlayerDataContainerView

    /**
     * Indicates whether the CloudPlayer is currently connected to a proxy server.
     */
    val connectedToProxy: Boolean

    /**
     * Indicates whether the player is currently connected to a real server (paper).
     */
    val connectedToServer: Boolean

    /**
     * Indicates whether the player is currently connected to a proxy or a server.
     *
     * This property returns `true` if the player is connected to either a proxy
     * or a server, and `false` otherwise.
     */
    val connected get() = connectedToProxy || connectedToServer

    /**
     * Edits the persistent data of the player.
     * This method will suspend until the data is fully edited and synced across the network.
     *
     * @param block The block to edit the data.
     */
    suspend fun editPersistentData(block: PersistentPlayerDataContainer.() -> Unit)

    /**
     * Connects the player to the specified server.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     *
     * @param server The server to connect to.
     * @return The result of the connection attempt.
     */
    suspend fun connectToServer(server: CloudServer): ConnectionResult

    /**
     * Connects the player to the specified server.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     *
     * @param group The group of the server to connect to.
     * @param server The name of the server to connect to.
     */
    suspend fun connectToServer(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in the specified group.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     *
     * @param group The group of the server to connect to.
     */
    suspend fun connectToServer(group: String): ConnectionResult

    /**
     * Connects the player to the specified server.
     * If the server is offline, full, the player will be queued to connect when the server is available.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     * > This method is suspended until the player is fully connected to the server which may take some time if the player is queued.
     *
     * @param server The server to connect to.
     */
    suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResult

    /**
     * Connects the player to the specified server.
     * If the server is offline, full, the player will be queued to connect when the server is available.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     * > This method is suspended until the player is fully connected to the server which may take some time if the player is queued.
     *
     * @param group The group of the server to connect to.
     * @param server The name of the server to connect to.
     */
    suspend fun connectToServerOrQueue(group: String, server: String): ConnectionResult

    /**
     * Connects the player to the server with the lowest player count in the specified group.
     * If the server is offline, full, the player will be queued to connect when the server is available.
     * This method will return a [ConnectionResult] indicating the result of the connection attempt.
     * > This method is suspended until the player is fully connected to the server which may take some time if the player is queued.
     *
     * @param group The group of the server to connect to.
     */
    suspend fun connectToServerOrQueue(group: String): ConnectionResult
}

/**
 * Represents the result of a connection attempt to a server.
 */
enum class ConnectionResult {
    /**
     * Represents a successful connection attempt to a server.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SUCCESS,
    /**
     * Represents a connection attempt where the specified server was not found.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SERVER_NOT_FOUND,
    /**
     * Represents a connection attempt where the specified server was found,
     * but is currently full and cannot accept new connections.
     *
     * @see CloudPlayer.connectToServer
     */
    SERVER_FULL,
    /**
     * Represents a connection attempt where the specified server was found but is currently offline.
     *
     * @see CloudPlayer.connectToServer
     * @see CloudPlayer.connectToServerOrQueue
     */
    SERVER_OFFLINE,
}