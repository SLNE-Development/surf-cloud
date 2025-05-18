package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.netty.network.protocol.await
import dev.slne.surf.cloud.api.common.netty.network.protocol.awaitOrThrow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.player.playtime.Playtime
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse.Status
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.playtime.PlaytimeEntry
import dev.slne.surf.cloud.core.common.player.playtime.PlaytimeImpl
import dev.slne.surf.cloud.core.common.player.ppdc.PersistentPlayerDataContainerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.QueueManagerImpl
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.resource.ResourcePackCallback
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.querz.nbt.tag.CompoundTag
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StandaloneCloudPlayerImpl(uuid: UUID, name: String, val ip: Inet4Address) :
    CommonCloudPlayerImpl(uuid, name) {

    companion object {
        private val log = logger()
        private val service by lazy { bean<CloudPlayerService>() }
        private val playtimeManager by lazy { bean<CloudPlayerPlaytimeManager>() }
        private val nettyServer by lazy { bean<NettyServerImpl>() }
    }

    @Volatile
    var proxyServer: StandaloneProxyCloudServerImpl? = null

    @Volatile
    var server: StandaloneCloudServerImpl? = null

    override val connectedToProxy get() = proxyServer != null
    override val connectedToServer get() = server != null
    val anyServer: ServerCommonCloudServer
        get() = server ?: proxyServer ?: error("Player is not connected to a server")

    @Deprecated("remove")
    @Volatile
    var connecting = false

    @Deprecated("remove")
    @Volatile
    var connectingToServer: StandaloneCloudServerImpl? = null

    private val ppdc = PersistentPlayerDataContainerImpl()

    private val ppdcMutex = Mutex()
    private var firstSeenCache: ZonedDateTime? = null

    var afk = false
        private set

    var sessionStartTime: ZonedDateTime = ZonedDateTime.now()

    fun savePlayerData(tag: CompoundTag) {
        if (!ppdc.empty) {
            tag.put("ppdc", ppdc.toTagCompound())
        }
    }

    fun readPlayerData(tag: CompoundTag) {
        val ppdcTag = tag.get("ppdc")
        if (ppdcTag is CompoundTag) {
            ppdc.fromTagCompound(ppdcTag)
        }
    }

    override fun isAfk(): Boolean {
        return afk
    }

    override suspend fun currentSessionDuration(): Duration {
        val duration = sessionStartTime.until(ZonedDateTime.now(), ChronoUnit.SECONDS)
        return duration.seconds
    }

    fun updateAfkStatus(newValue: Boolean) {
        if (newValue == afk) return
        afk = newValue

        nettyServer.connection.broadcast(UpdateAFKStatePacket(uuid, afk))

        sendText {
            appendPrefix()
            info("Du bist nun ")
            if (afk) {
                info("AFK und erhältst keine weiteren Paychecks.")
            } else {
                info("nicht mehr AFK.")
            }
        }
    }

    override suspend fun <R> withPersistentData(block: PersistentPlayerDataContainer.() -> R): R =
        ppdcMutex.withLock {
            ppdc.block()
        }

    override fun disconnect(reason: Component) {
        val connection = proxyServer?.connection ?: server?.connection
        connection?.send(DisconnectPlayerPacket(uuid, reason))
    }

    override fun disconnectSilent() {
        server?.connection?.send(SilentDisconnectPlayerPacket(uuid))
    }

    suspend fun getPersistentData() = ppdcMutex.withLock { ppdc.toTagCompound() }
    suspend fun updatePersistentData(tag: CompoundTag) =
        ppdcMutex.withLock { ppdc.fromTagCompound(tag) }

    override suspend fun latestIpAddress(): Inet4Address {
        return ip
    }

    override suspend fun playtime(): Playtime {
        val dbPlaytimes = service.loadPlaytimeEntries(uuid)
        val memoryPlaytimes = createMemoryEntriesFromSessions()
        dbPlaytimes.removeIf { db -> memoryPlaytimes.any { mem -> db.id == mem.id } }
        val allPlaytimes = dbPlaytimes + memoryPlaytimes

        if (allPlaytimes.isEmpty()) {
            return PlaytimeImpl.EMPTY
        }

        return PlaytimeImpl(allPlaytimes.toObjectList())
    }

    private suspend fun createMemoryEntriesFromSessions(): ObjectList<PlaytimeEntry> {
        val session = playtimeManager.playtimeSessionFor(uuid) ?: return mutableObjectListOf()
        return mutableObjectListOf(
            PlaytimeEntry(
                id = session.sessionId,
                category = session.category,
                server = session.serverName,
                durationSeconds = session.accumulatedSeconds,
                createdAt = session.startTime,
            )
        )
    }

    override suspend fun lastServerRaw(): String {
        return anyServer.name
    }

    override suspend fun firstSeen(): ZonedDateTime? {
        return firstSeenCache ?: service.findFirstSeen(uuid).also {
            firstSeenCache = it
        }
    }

    override suspend fun nameHistory(): NameHistory {
        return service.findNameHistories(uuid)
    }

    override suspend fun displayName(): Component = ClientboundRequestDisplayNamePacket(uuid)
        .fireAndAwaitUrgent(anyServer.connection)?.displayName
        ?: error("Failed to get display name (probably timed out)")

    override suspend fun name(): String {
        return name
    }

    override suspend fun connectToServer(server: CloudServer): ConnectionResultEnum {
        check(server is StandaloneCloudServerImpl) { "Server must be a StandaloneCloudServerImpl" }

        return server.pullPlayer(this)

//        if (connecting) {
//            return ConnectionResultEnum.CONNECTION_IN_PROGRESS to null
//        }
//
//        connecting = true
//
//        // is user connected through proxy?
//        // yes
//        //   -> Is new server managed by the same proxy?
//        //      yes
//        //        -> Send connect packet
//        //      no
//        //        -> Return ConnectionResult.CANNOT_SWITCH_PROXY
//        // no
//        //   -> try send transfer packet
//        //   -> if failed, return ConnectionResult.OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET
//        //   -> if succeeded, return ConnectionResult.SUCCESS
//
//        val proxy = proxyServer
//        if (proxy != null) {
//            connectingToServer = server
//            return switchServerUnderSameProxy(proxy, server).also {
//                connecting = false; connectingToServer = null
//            }
//        }
//
//        error("NOT SUPPORTED")
//        return switchServerUnderNoProxy(server).also { connecting = false }
    }

    private suspend fun switchServerUnderSameProxy(
        proxy: StandaloneProxyCloudServerImpl,
        target: StandaloneCloudServerImpl
    ): ConnectionResultEnum {
        val underSameProxy =
            ClientboundIsServerManagedByThisProxyPacket(target.connection.virtualHost)
                .fireAndAwait(proxy.connection)
                ?.isManagedByThisProxy
                ?: return ConnectionResultEnum.CANNOT_COMMUNICATE_WITH_PROXY

        if (!underSameProxy) {
            return ConnectionResultEnum.CANNOT_SWITCH_PROXY
        }

        val response = ClientboundTransferPlayerPacket(uuid, target.connection.virtualHost)
            .fireAndAwait(target.connection, Duration.INFINITE)
            ?: error("Failed to send transfer packet")

        return when (response.status) {
            Status.SUCCESS -> ConnectionResultEnum.SUCCESS
            Status.ALREADY_CONNECTED -> ConnectionResultEnum.ALREADY_CONNECTED
            Status.CONNECTION_IN_PROGRESS -> ConnectionResultEnum.CONNECTION_IN_PROGRESS
            Status.CONNECTION_CANCELLED -> ConnectionResultEnum.CONNECTION_CANCELLED
            Status.SERVER_DISCONNECTED -> ConnectionResultEnum.SERVER_DISCONNECTED
        }
    }

    override suspend fun connectToServerOrQueue(
        server: CloudServer,
        sendQueuedMessage: Boolean
    ): ConnectionResultEnum {
        check(server is StandaloneCloudServerImpl) { "Server must be a StandaloneCloudServerImpl" }
        connecting = true
        connectingToServer = server

        val manager = bean<QueueManagerImpl>()
        val deferred = manager.queueForServer(this, server, sendQueuedMessage)
        deferred.invokeOnCompletion { connecting = false; connectingToServer = null }

        return deferred.await()
    }

    override suspend fun connectToServerOrQueue(
        group: String,
        sendQueuedMessage: Boolean
    ): ConnectionResultEnum {
        connecting = true

        val manager = bean<QueueManagerImpl>()
        val deferred = manager.queueForGroup(this, group, sendQueuedMessage)
        deferred.invokeOnCompletion { connecting = false; connectingToServer = null }

        return deferred.await()
    }

    override fun isOnServer(server: CloudServer): Boolean {
        if (server !is StandaloneCloudServerImpl) {
            return false
        }

        return server == this.server
    }

    override fun isInGroup(group: String): Boolean {
        val server = server
        if (server == null) {
            return false
        }

        return server.group == group
    }

    override suspend fun getLuckpermsMetaData(key: String): String? {
        return RequestLuckpermsMetaDataPacket(uuid, key).fireAndAwait(anyServer.connection)?.data
    }


    @Deprecated("Deprecated in Java")
    @Suppress("UnstableApiUsage", "DEPRECATION")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        send(ClientboundSendMessagePacket(uuid, message))
    }

    override fun sendMessage(
        message: ComponentLike,
        permission: String
    ) {
        send(ClientboundSendMessagePacket(uuid, message.asComponent(), permission))
    }

    override fun sendActionBar(message: Component) {
        send(ClientboundSendActionBarPacket(uuid, message))
    }

    override fun sendPlayerListHeaderAndFooter(
        header: Component,
        footer: Component
    ) {
        send(
            ClientboundSendPlayerListHeaderAndFooterPacket(
                uuid,
                header,
                footer
            )
        )
    }

    override fun showTitle(title: Title) {
        send(ClientboundShowTitlePacket(uuid, title))
    }

    override fun <T : Any> sendTitlePart(
        part: TitlePart<T>,
        value: T
    ) {
        send(ClientboundSendTitlePartPacket(uuid, part, value))
    }

    override fun clearTitle() {
        send(ClientboundClearTitlePacket(uuid))
    }

    override fun resetTitle() {
        send(ClientboundResetTitlePacket(uuid))
    }

    override fun showBossBar(bar: BossBar) {
        send(ClientboundShowBossBarPacket(uuid, bar))
    }

    override fun hideBossBar(bar: BossBar) {
        send(ClientboundHideBossBarPacket(uuid, bar))
    }

    override fun playSound(
        sound: Sound,
        x: Double,
        y: Double,
        z: Double
    ) {
        send(ClientboundPlaySoundPacket(uuid, sound, x, y, z))
    }

    override fun playSound(
        sound: Sound,
        emitter: Emitter
    ) {
        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }

        send(ClientboundPlaySoundPacket(uuid, sound, emitter))
    }

    override fun playSound(
        sound: Sound,
        emitter: Emitter,
        permission: String
    ) {
        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }

        send(ClientboundPlaySoundPacket(uuid, sound, emitter, permission))
    }

    override suspend fun hasPermission(permission: String): Boolean {
        return RequestPlayerPermissionPacket(uuid, permission).awaitOrThrow(anyServer.connection)
    }

    override fun playSound(sound: Sound) {
        send(ClientboundPlaySoundPacket(uuid, sound))
    }

    override fun stopSound(stop: SoundStop) {
        send(ClientboundStopSoundPacket(uuid, stop))
    }

    override fun openBook(book: Book) {
        send(ClientboundOpenBookPacket(uuid, book))
    }

    override fun sendResourcePacks(request: ResourcePackRequest) {
        if (request.callback() != ResourcePackCallback.noOp()) {
            log.atWarning()
                .atMostEvery(30, TimeUnit.SECONDS)
                .log("Resource pack callback is not supported in standalone mode. Ignoring.")
        }

        send(ClientboundSendResourcePacksPacket(uuid, request))
    }

    override fun removeResourcePacks(id: UUID, vararg others: UUID) {
        send(ClientboundRemoveResourcePacksPacket(uuid, id, *others))
    }

    override fun clearResourcePacks() {
        send(ClientboundClearResourcePacksPacket(uuid))
    }

    override suspend fun teleport(
        location: TeleportLocation,
        teleportCause: TeleportCause,
        vararg flags: TeleportFlag
    ): Boolean {
        val server = server ?: return false

        return TeleportPlayerPacket(
            uuid,
            location,
            teleportCause,
            *flags
        ).fireAndAwait(server.connection)?.result == true
    }

    override suspend fun teleport(target: CloudPlayer): Boolean {
        require(target is StandaloneCloudPlayerImpl) { "Target must be a StandaloneCloudPlayerImpl" }

        val targetServer = target.server
        if (targetServer == null || targetServer != this.server) {
            val result = this.connectToServer(targetServer ?: return false)
            if (!result.isSuccess) {
                this.sendMessage(result.message)
                return false
            }
        }

        val result = TeleportPlayerToPlayerPacket(
            this.uuid,
            target.uuid
        ).await(targetServer.connection) == true
        return result
    }

    private fun send(packet: NettyPacket) {
        anyServer.connection.send(packet)
    }
}