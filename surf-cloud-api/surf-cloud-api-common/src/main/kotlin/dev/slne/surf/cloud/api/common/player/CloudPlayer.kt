package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.bytebufserializer.Buf
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud.CloudPlayerSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import io.netty.buffer.ByteBuf
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
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
@Serializable(with = CloudPlayerSerializer::class)
interface CloudPlayer : Audience, OfflineCloudPlayer { // TODO: conversation but done correctly?
    val name: String

    override suspend fun latestIpAddress(): Inet4Address
    override suspend fun lastServerRaw(): String
    override suspend fun lastServer(): CloudServer = currentServer()
    fun currentServer(): CloudServer

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

    fun isAfk(): Boolean
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
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(server: CloudServer): ConnectionResultEnum

    /**
     * Connects the player to a server by its group and name.
     *
     * @param group The server group name.
     * @param server The target server name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String, server: String): ConnectionResultEnum

    /**
     * Connects the player to the server with the lowest player count in the specified group.
     *
     * @param group The server group name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String): ConnectionResultEnum

    /**
     * Connects the player to the specified server or places them in a queue if unavailable.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(
        server: CloudServer,
        sendQueuedMessage: Boolean = true
    ): ConnectionResultEnum

    /**
     * Connects the player to a server by group and name or places them in a queue if unavailable.
     *
     * @param group The server group name.
     * @param server The target server name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(
        group: String,
        server: String,
        sendQueuedMessage: Boolean = true
    ): ConnectionResultEnum

    /**
     * Connects the player to the server with the lowest player count in a group or queues them if unavailable.
     *
     * @param group The server group name.
     * @return A [ConnectionResult] indicating the result of the connection attempt.
     */
    suspend fun connectToServerOrQueue(
        group: String,
        sendQueuedMessage: Boolean = true
    ): ConnectionResultEnum

    fun isOnServer(server: CloudServer): Boolean
    fun isInGroup(group: String): Boolean

    /**
     * Disconnects the player from the network with a specified reason.
     *
     * @param reason The reason for the disconnection.
     */
    fun disconnect(reason: Component)

    /**
     * Disconnects the player from the network silently.
     *
     * The player will think they are timed out.
     */
    fun disconnectSilent()

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
        location: WorldLocation,
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
    ) = teleport(WorldLocation(world, x, y, z, yaw, pitch), teleportCause, *flags)

    suspend fun teleport(target: CloudPlayer): Boolean

    override suspend fun displayName(): Component
    override suspend fun name(): String

    fun sendMessage(message: ComponentLike, permission: String)
    fun playSound(sound: Sound, emitter: Sound.Emitter, permission: String)

    suspend fun hasPermission(permission: String): Boolean

    companion object {
        operator fun get(uuid: UUID) = CloudPlayerManager.getPlayer(uuid)
        operator fun get(name: String) = CloudPlayerManager.getPlayer(name)
        fun all() = CloudPlayerManager.getOnlinePlayers()
    }
}

/**
 * Enum like class representing the result of a player's connection attempt to a server.
 */
@Suppress("ClassName")
@Serializable
sealed class ConnectionResultEnum(
    val message: @Contextual Component,
    val isSuccess: Boolean = false
) {
    @Serializable
    object SUCCESS : ConnectionResultEnum(
        buildText { success("Du hast dich erfolgreich Verbunden.") },
        isSuccess = true
    )

    @Serializable
    class SERVER_NOT_FOUND(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Der Server ")
            variableValue(serverName)
            error(" wurde nicht gefunden.")
        }
    )

    @Serializable
    object SERVER_FULL : ConnectionResultEnum(
        buildText { error("Der Server ist voll.") }
    )

    @Serializable
    object CATEGORY_FULL : ConnectionResultEnum(
        buildText { error("Die Kategorie ist voll.") }
    )

    @Serializable
    object SERVER_OFFLINE : ConnectionResultEnum(
        buildText { error("Der Server ist offline.") }
    )

    @Serializable
    object ALREADY_CONNECTED : ConnectionResultEnum(
        buildText { error("Du bist bereits mit diesem Server verbunden.") }
    )

    @Serializable
    object CANNOT_SWITCH_PROXY : ConnectionResultEnum(
        buildText { error("Du kannst nicht zu diesem Server wechseln, da dieser unter einem anderen Proxy läuft.") }
    )

    @Serializable
    object OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET : ConnectionResultEnum(
        buildText { error("Der Server kann das Transfer-Paket nicht akzeptieren.") }
    )

    @Serializable
    object CANNOT_COMMUNICATE_WITH_PROXY : ConnectionResultEnum(
        buildText { error("Der Proxy kann nicht erreicht werden.") }
    )

    @Serializable
    object CONNECTION_IN_PROGRESS : ConnectionResultEnum(
        buildText { error("Du versuchst bereits eine Verbindung zu einem Server herzustellen.") }
    )

    @Serializable
    object CONNECTION_CANCELLED : ConnectionResultEnum(
        buildText { error("Die Verbindung wurde abgebrochen.") }
    )

    @Serializable
    object SERVER_DISCONNECTED : ConnectionResultEnum(
        buildText { error("Der Server hat die Verbindung getrennt.") }
    )

    @Serializable
    object CANNOT_CONNECT_TO_PROXY : ConnectionResultEnum(
        buildText { error("Der Proxy kann nicht erreicht werden.") }
    )

    @Serializable
    class ALREADY_QUEUED(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Du bist bereits in der Warteschlange für den Server ")
            variableValue(serverName)
            error(".")
        }
    )

    @Serializable
    class QUEUE_SUSPENDED(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Die Warteschlange für den Server ")
            variableValue(serverName)
            error(" ist derzeit pausiert.")
        }
    )

    @Serializable
    class MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED(
        val latestFailure: ConnectionResultEnum,
        val serverName: String,
        val maxRetries: Int
    ) : ConnectionResultEnum(buildText {
        appendPrefix()
        append {
            error("Du konntest nicht mit dem Server ")
            variableValue(serverName)
            error(" verbunden werden, ")
        }
        appendNewPrefixedLine()
        error("da das Maximum an Versuchen ($maxRetries) erreicht wurde.")
    })

    @Serializable
    object DISCONNECTED : ConnectionResultEnum(
        buildText {
            error("Du hast die Verbindung getrennt.")
        }
    )

    @Serializable
    class QUEUE_SWAPPED(val oldQueue: String, val newQueue: String) : ConnectionResultEnum(
        buildText {
            error("Du bist von der Warteschlange ")
            variableValue(oldQueue)
            error(" in die Warteschlange ")
            variableValue(newQueue)
            error(" gewechselt.")
        }
    )

    @Serializable
    object SERVER_SWITCHED : ConnectionResultEnum(
        buildText {
            error("Du bist auf einen anderen Server gewechselt.")
        }
    )

    companion object {
        val STREAM_CODEC = streamCodec<ByteBuf, ConnectionResultEnum>({ buf, value ->
            Buf.encodeToBuf(buf, value)
        }, { buf ->
            Buf.decodeFromBuf(buf)
        })
    }
}