package dev.slne.surf.cloud.api.common.player

import dev.slne.surf.cloud.api.common.netty.network.codec.*
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud.CloudPlayerSerializer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.api.common.player.toast.NetworkToast
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import io.netty.buffer.ByteBuf
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
interface CloudPlayer : Audience, OfflineCloudPlayer {
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

    val persistentData: PersistentPlayerDataContainerView

    /**
     * Performs modifications on the player's persistent data container.
     *
     * @param block A block to modify the persistent data container.
     * @return The result of the block execution.
     */
    fun <R> editPdc(block: PersistentPlayerDataContainer.() -> R): R

    @Deprecated("Use non-suspending editPdc method instead", ReplaceWith("editPdc(block)"))
    suspend fun <R> withPersistentData(block: PersistentPlayerDataContainer.() -> R): R =
        editPdc(block)

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
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String, server: String): ConnectionResultEnum

    /**
     * Connects the player to the server with the lowest player count in the specified group.
     *
     * @param group The server group name.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
     */
    suspend fun connectToServer(group: String): ConnectionResultEnum

    /**
     * Connects the player to the specified server or places them in a queue if unavailable.
     *
     * @param server The target server to connect to.
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
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
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
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
     * @return A [ConnectionResultEnum] indicating the result of the connection attempt.
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

    fun sendToast(toast: NetworkToast)
    fun sendToast(builder: NetworkToast.Builder.() -> Unit) =
        sendToast(NetworkToast.create(builder))

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
sealed class ConnectionResultEnum(
    val message: Component,
    val isSuccess: Boolean = false
) {
    object SUCCESS : ConnectionResultEnum(
        buildText { success("Du hast dich erfolgreich Verbunden.") },
        isSuccess = true
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 0
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    class SERVER_NOT_FOUND(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Der Server ")
            variableValue(serverName)
            error(" wurde nicht gefunden.")
        }
    ) {
        companion object : SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
            override val id = 1
            override val STREAM_CODEC =
                ByteBufCodecs.STRING_CODEC.map(::SERVER_NOT_FOUND) { it.serverName }
        }
    }

    object SERVER_FULL : ConnectionResultEnum(
        buildText { error("Der Server ist voll.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 2
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CATEGORY_FULL : ConnectionResultEnum(
        buildText { error("Die Kategorie ist voll.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 3
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object SERVER_OFFLINE : ConnectionResultEnum(
        buildText { error("Der Server ist offline.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 4
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object ALREADY_CONNECTED : ConnectionResultEnum(
        buildText { error("Du bist bereits mit diesem Server verbunden.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 5
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CANNOT_SWITCH_PROXY : ConnectionResultEnum(
        buildText { error("Du kannst nicht zu diesem Server wechseln, da dieser unter einem anderen Proxy läuft.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 6
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET : ConnectionResultEnum(
        buildText { error("Der Server kann das Transfer-Paket nicht akzeptieren.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 7
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CANNOT_COMMUNICATE_WITH_PROXY : ConnectionResultEnum(
        buildText { error("Der Proxy kann nicht erreicht werden.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 8
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CONNECTION_IN_PROGRESS : ConnectionResultEnum(
        buildText { error("Du versuchst bereits eine Verbindung zu einem Server herzustellen.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 9
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CONNECTION_CANCELLED : ConnectionResultEnum(
        buildText { error("Die Verbindung wurde abgebrochen.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 10
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object SERVER_DISCONNECTED : ConnectionResultEnum(
        buildText { error("Der Server hat die Verbindung getrennt.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 11
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    object CANNOT_CONNECT_TO_PROXY : ConnectionResultEnum(
        buildText { error("Der Proxy kann nicht erreicht werden.") }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 12
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    class ALREADY_QUEUED(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Du bist bereits in der Warteschlange für den Server ")
            variableValue(serverName)
            error(".")
        }
    ) {
        companion object : SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
            override val id = 13
            override val STREAM_CODEC =
                ByteBufCodecs.STRING_CODEC.map(::ALREADY_QUEUED) { it.serverName }
        }
    }

    class QUEUE_SUSPENDED(val serverName: String) : ConnectionResultEnum(
        buildText {
            error("Die Warteschlange für den Server ")
            variableValue(serverName)
            error(" ist derzeit pausiert.")
        }
    ) {
        companion object : SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
            override val id = 14
            override val STREAM_CODEC =
                ByteBufCodecs.STRING_CODEC.map(::QUEUE_SUSPENDED) { it.serverName }
        }
    }

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
    }) {
        companion object : SealedVariantCodecProviderBufComplex<ConnectionResultEnum> {
            override val id = 15
            override fun localCodec(
                parent: StreamCodec<ByteBuf, ConnectionResultEnum>
            ) = StreamCodec.composite(
                parent,
                MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED::latestFailure,
                ByteBufCodecs.STRING_CODEC,
                MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED::serverName,
                ByteBufCodecs.VAR_INT_CODEC,
                MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED::maxRetries,
                ::MAX_QUEUE_CONNECTION_ATTEMPTS_REACHED
            )
        }
    }

    object DISCONNECTED : ConnectionResultEnum(
        buildText {
            error("Du hast die Verbindung getrennt.")
        }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 16
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    class QUEUE_SWAPPED(val oldQueue: String, val newQueue: String) : ConnectionResultEnum(
        buildText {
            error("Du bist von der Warteschlange ")
            variableValue(oldQueue)
            error(" in die Warteschlange ")
            variableValue(newQueue)
            error(" gewechselt.")
        }
    ) {
        companion object : SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
            override val id = 17
            override val STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_CODEC,
                QUEUE_SWAPPED::oldQueue,
                ByteBufCodecs.STRING_CODEC,
                QUEUE_SWAPPED::newQueue,
                ::QUEUE_SWAPPED
            )
        }
    }

    object SERVER_SWITCHED : ConnectionResultEnum(
        buildText {
            error("Du bist auf einen anderen Server gewechselt.")
        }
    ), SealedVariantCodecProviderBufSimple<ConnectionResultEnum> {
        override val id = 18
        override val STREAM_CODEC = streamCodecUnitSimple(this)
    }

    companion object {
        val STREAM_CODEC =
            StreamCodec.forSealedClassAuto(ByteBufCodecs.VAR_INT_CODEC, ConnectionResultEnum::class)
    }
}