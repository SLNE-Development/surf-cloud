package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.server.CloudServer
import net.kyori.adventure.audience.Audience
import java.util.*

interface CloudPlayer: Audience {
    val uuid: UUID
    val persistentDataView: PersistentPlayerDataContainerView

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

enum class ConnectionResult {
    SUCCESS,
    SERVER_NOT_FOUND,
    SERVER_FULL,
    SERVER_OFFLINE,
}