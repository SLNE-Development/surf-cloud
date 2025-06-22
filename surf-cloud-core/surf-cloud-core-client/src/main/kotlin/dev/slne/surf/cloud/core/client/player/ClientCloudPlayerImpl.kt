package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.awaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwait
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_URGENT_TIMEOUT
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.player.playtime.Playtime
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.client.util.luckperms
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataPacket.DataRequestType
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataResponse.*
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.ppdc.PersistentPlayerDataContainerImpl
import dev.slne.surf.cloud.core.common.util.hasPermissionPlattform
import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import dev.slne.surf.surfapi.core.api.nbt.fast
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.luckperms.api.model.user.User
import net.luckperms.api.platform.PlayerAdapter
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataResponse.NameHistory as NameHistoryResponse

abstract class ClientCloudPlayerImpl<PlatformPlayer : Audience>(uuid: UUID, name: String) :
    CommonCloudPlayerImpl(uuid, name) {
    @Volatile
    var proxyServerUid: Long? = null

    @Volatile
    var serverUid: Long? = null

    @Volatile
    var afk: Boolean = false

    override val connectedToProxy get() = proxyServerUid != null

    override val connectedToServer get() = serverUid != null

    /**
     * The audience for this player. If the player is on this server, this will point to
     * the bukkit / velocity player. Otherwise packets will be sent to the player via the network.
     */
    protected abstract val audience: PlatformPlayer?

    protected abstract val platformClass: Class<PlatformPlayer>

    override suspend fun latestIpAddress(): Inet4Address {
        return request<IpAddress>(DataRequestType.LATEST_IP_ADDRESS).ip
            ?: error("Failed to get IP address")
    }

    override suspend fun lastServerRaw(): String {
        return request<LastServer>(DataRequestType.LAST_SERVER).server
            ?: error("Failed to get last server")
    }

    override fun currentServer(): CloudServer {
        val server = serverManagerImpl.getServerByIdUnsafe(
            serverUid ?: error("Player is not connected to a server")
        ) ?: error("Server not found for UID: $serverUid")

        require(server is CloudServer) { "Expected CloudServer, but got ${server::class.simpleName}" }

        return server
    }

    override suspend fun nameHistory(): NameHistory {
        return request<NameHistoryResponse>(DataRequestType.NAME_HISTORY).history
    }

    override suspend fun firstSeen(): ZonedDateTime? {
        return request<FirstSeen>(DataRequestType.FIRST_SEEN).firstSeen
    }

    override fun isAfk(): Boolean {
        return afk
    }

    override suspend fun currentSessionDuration(): Duration {
        return request<PlaytimeSession>(DataRequestType.PLAYTIME_SESSION).playtime
    }

    override fun isOnServer(server: CloudServer): Boolean {
        return server.uid == serverUid
    }

    override fun isInGroup(group: String): Boolean {
        val currentServer = serverUid
        return currentServer != null && serverManagerImpl.getServerByIdUnsafe(currentServer)?.group?.equals(
            group,
            ignoreCase = true
        ) == true
    }

    override suspend fun <R> withPersistentData(block: PersistentPlayerDataContainer.() -> R): R {
        val response = ServerboundRequestPlayerPersistentDataContainer(uuid).fireAndAwaitOrThrow()

        val nbt = response.nbt
        val container = PersistentPlayerDataContainerImpl(nbt.fast(true))
        val result = container.block()

        ServerboundPlayerPersistentDataContainerUpdatePacket(
            uuid,
            response.verificationId,
            container.toTagCompound()
        ).fireAndForget()

        return result
    }

    override suspend fun displayName(): Component {
        val localName = audience?.getPointer(Identity.DISPLAY_NAME)
        if (localName != null) {
            return localName
        }

        return ServerboundRequestDisplayNamePacket(uuid).fireAndAwait(DEFAULT_URGENT_TIMEOUT)?.displayName
            ?: error("Failed to get display name (probably timed out)")
    }

    override suspend fun playtime(): Playtime {
        return request<ServerboundRequestPlayerDataResponse.Playtime>(DataRequestType.PLAYTIME).playtime
    }

    override suspend fun name(): String {
        val localName = audience?.getPointer(Identity.NAME)
        if (localName != null) {
            return localName
        }

        return request<Name>(DataRequestType.NAME).name ?: error("Failed to get name")
    }

    override suspend fun connectToServer(server: CloudServer): ConnectionResultEnum {
        return ServerboundConnectPlayerToServerPacket(
            uuid,
            server.uid,
            false
        ).fireAndAwaitOrThrow(1.days).result
    }

    override suspend fun connectToServerOrQueue(
        server: CloudServer,
        sendQueuedMessage: Boolean
    ): ConnectionResultEnum {
        return ServerboundConnectPlayerToServerPacket(
            uuid,
            server.uid,
            sendQueuedMessage
        ).fireAndAwaitOrThrow(1.days).result
    }

    override suspend fun connectToServerOrQueue(
        group: String,
        sendQueuedMessage: Boolean
    ): ConnectionResultEnum {
        return ServerboundQueuePlayerToGroupPacket(
            uuid,
            group,
            sendQueuedMessage
        ).fireAndAwaitOrThrow(1.days).result
    }

    override suspend fun getLuckpermsMetaData(key: String): String? {
        val player = audience
        if (player != null) {
            return withLuckpermsAdapter { it.getMetaData(player).getMetaValue(key) }
        }

        return RequestLuckpermsMetaDataPacket(uuid, key).fireAndAwait()?.data
    }

    @Deprecated("Deprecated in Java")
    @Suppress("UnstableApiUsage", "DEPRECATION")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        val audience = audience
        if (audience != null) {
            audience.sendMessage(source, message, type)
            return
        }

        ServerboundSendMessagePacket(uuid, message).fireAndForget()
    }

    override fun sendMessage(
        message: ComponentLike,
        permission: String
    ) {
        val audience = audience
        if (audience != null) {
            if (audience.hasPermissionPlattform(permission)) {
                audience.sendMessage(message)
            }
            return
        }

        ServerboundSendMessagePacket(uuid, message.asComponent(), permission).fireAndForget()
    }

    override fun sendActionBar(message: Component) {
        val audience = audience
        if (audience != null) {
            audience.sendActionBar(message)
            return
        }

        ServerboundSendActionBarPacket(uuid, message).fireAndForget()
    }

    override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
        val audience = audience
        if (audience != null) {
            audience.sendPlayerListHeaderAndFooter(header, footer)
            return
        }

        ServerboundSendPlayerListHeaderAndFooterPacket(uuid, header, footer).fireAndForget()
    }

    override fun showTitle(title: Title) {
        val audience = audience
        if (audience != null) {
            audience.showTitle(title)
            return
        }

        ServerboundShowTitlePacket(uuid, title).fireAndForget()
    }

    override fun <T : Any> sendTitlePart(part: TitlePart<T>, value: T) {
        val audience = audience
        if (audience != null) {
            audience.sendTitlePart(part, value)
            return
        }

        ServerboundSendTitlePartPacket(uuid, part, value).fireAndForget()
    }

    override fun clearTitle() {
        val audience = audience
        if (audience != null) {
            audience.clearTitle()
            return
        }

        ServerboundClearTitlePacket(uuid).fireAndForget()
    }

    override fun resetTitle() {
        val audience = audience
        if (audience != null) {
            audience.resetTitle()
            return
        }

        ServerboundResetTitlePacket(uuid).fireAndForget()
    }

    override fun showBossBar(bar: BossBar) {
        val audience = audience
        if (audience != null) {
            audience.showBossBar(bar)
            return
        }

        ServerboundShowBossBarPacket(uuid, bar).fireAndForget()
    }

    override fun hideBossBar(bar: BossBar) {
        val audience = audience
        if (audience != null) {
            audience.hideBossBar(bar)
            return
        }

        ServerboundHideBossBarPacket(uuid, bar).fireAndForget()
    }

    override fun playSound(sound: Sound) {
        val audience = audience
        if (audience != null) {
            audience.playSound(sound)
            return
        }

        ServerboundPlaySoundPacket(uuid, sound).fireAndForget()
    }

    override fun playSound(sound: Sound, emitter: Emitter) {
        val audience = audience
        if (audience != null) {
            audience.playSound(sound, emitter)
            return
        }

        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }

        ServerboundPlaySoundPacket(uuid, sound, emitter).fireAndForget()
    }

    override fun playSound(
        sound: Sound,
        emitter: Emitter,
        permission: String
    ) {
        val audience = audience
        if (audience != null) {
            if (audience.hasPermissionPlattform(permission)) {
                audience.playSound(sound, emitter)
            }
            return
        }

        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }

        ServerboundPlaySoundPacket(uuid, sound, emitter, permission).fireAndForget()
    }

    override suspend fun hasPermission(permission: String): Boolean {
        val audience = audience
        if (audience != null) {
            return audience.hasPermissionPlattform(permission)
        }

        return RequestPlayerPermissionPacket(uuid, permission).awaitOrThrow()
    }

    override fun playSound(sound: Sound, x: Double, y: Double, z: Double) {
        val audience = audience
        if (audience != null) {
            audience.playSound(sound, x, y, z)
            return
        }

        ServerboundPlaySoundPacket(uuid, sound, x, y, z).fireAndForget()
    }

    override fun stopSound(stop: SoundStop) {
        val audience = audience
        if (audience != null) {
            audience.stopSound(stop)
            return
        }

        ServerboundStopSoundPacket(uuid, stop).fireAndForget()
    }

    override fun openBook(book: Book) {
        val audience = audience
        if (audience != null) {
            audience.openBook(book)
            return
        }

        ServerboundOpenBookPacket(uuid, book).fireAndForget()
    }

    override fun sendResourcePacks(request: ResourcePackRequest) {
        val audience = audience
        if (audience != null) {
            audience.sendResourcePacks(request)
            return
        }

        ServerboundSendResourcePacksPacket(uuid, request).fireAndForget()
    }

    override fun removeResourcePacks(id: UUID, vararg others: UUID) {
        val audience = audience
        if (audience != null) {
            audience.removeResourcePacks(id, *others)
            return
        }

        ServerboundRemoveResourcePacksPacket(uuid, id, *others).fireAndForget()
    }

    override fun clearResourcePacks() {
        val audience = audience
        if (audience != null) {
            audience.clearResourcePacks()
            return
        }

        ServerboundClearResourcePacksPacket(uuid).fireAndForget()
    }

    override suspend fun teleport(
        location: TeleportLocation,
        teleportCause: TeleportCause,
        vararg flags: TeleportFlag
    ) = TeleportPlayerPacket(uuid, location, teleportCause, *flags).fireAndAwaitOrThrow().result

    override suspend fun teleport(target: CloudPlayer): Boolean {
        return TeleportPlayerToPlayerPacket(uuid, target.uuid).awaitOrThrow()
    }

    protected fun <R> withLuckpermsOrThrow(block: (User) -> R): R {
        val user = luckperms.userManager.getUser(uuid)
            ?: error("User not found in LuckPerms! Are you sure the player is online?")
        return block(user)
    }

    protected fun <R> withLuckpermsAdapter(block: (PlayerAdapter<PlatformPlayer>) -> R): R {
        return block(luckperms.getPlayerAdapter(platformClass))
    }

    private suspend inline fun <reified T : DataResponse> request(
        type: DataRequestType
    ): T {
        val response = ServerboundRequestPlayerDataPacket(uuid, type).fireAndAwaitOrThrow().data
        if (response !is T) {
            error("Unexpected response type: ${response::class.simpleName}")
        }
        return response
    }
}