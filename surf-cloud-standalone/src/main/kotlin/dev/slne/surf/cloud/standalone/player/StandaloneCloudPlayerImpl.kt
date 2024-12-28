package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse.Status
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.ppdc.PersistentPlayerDataContainerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.querz.nbt.tag.CompoundTag
import java.util.*
import kotlin.time.Duration

class StandaloneCloudPlayerImpl(uuid: UUID) : CommonCloudPlayerImpl(uuid) {
    @Volatile
    var proxyServer: StandaloneProxyCloudServerImpl? = null

    @Volatile
    var server: StandaloneCloudServerImpl? = null

    override val connectedToProxy get() = proxyServer != null
    override val connectedToServer get() = server != null
    private val anyServer: ServerCommonCloudServer
        get() = server ?: proxyServer ?: error("Player is not connected to a server")

    @Volatile
    private var connecting = false

    @Volatile
    var connectionQueueCallback: CompletableDeferred<ConnectionResult>? = null
        private set

    @Volatile
    var connectingToServer: StandaloneCloudServerImpl? = null
        private set

    private val ppdc = PersistentPlayerDataContainerImpl()

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

    fun writeTestToPdc() {
        ppdc.putInt(Key.key("test"), 123)
        ppdc.putString(Key.key("test2"), "test")
    }

    override suspend fun displayName(): Component = ClientboundRequestDisplayNamePacket(uuid)
        .fireAndAwaitUrgent(anyServer.connection)?.displayName
        ?: error("Failed to get display name (probably timed out)")

    override suspend fun connectToServer(server: CloudServer): ConnectionResult {
        check(server is StandaloneCloudServerImpl) { "Server must be a StandaloneCloudServerImpl" }

        if (connecting) {
            return ConnectionResultEnum.CONNECTION_IN_PROGRESS to null
        }

        connecting = true

        // is user connected through proxy?
        // yes
        //   -> Is new server managed by the same proxy?
        //      yes
        //        -> Send connect packet
        //      no
        //        -> Return ConnectionResult.CANNOT_SWITCH_PROXY
        // no
        //   -> try send transfer packet
        //   -> if failed, return ConnectionResult.OTHER_SERVER_CANNOT_ACCEPT_TRANSFER_PACKET
        //   -> if succeeded, return ConnectionResult.SUCCESS

        val proxy = proxyServer
        if (proxy != null) {
            connectingToServer = server
            return switchServerUnderSameProxy(proxy, server).also {
                connecting = false; connectingToServer = null
            }
        }

        error("NOT SUPPORTED")
//        return switchServerUnderNoProxy(server).also { connecting = false }
    }

    private suspend fun switchServerUnderSameProxy(
        proxy: StandaloneProxyCloudServerImpl,
        target: StandaloneCloudServerImpl
    ): ConnectionResult {
        val underSameProxy =
            ClientboundIsServerManagedByThisProxyPacket(target.connection.virtualHost)
                .fireAndAwait(proxy.connection)
                ?.isManagedByThisProxy
                ?: return ConnectionResultEnum.CANNOT_COMMUNICATE_WITH_PROXY to null

        if (!underSameProxy) {
            return ConnectionResultEnum.CANNOT_SWITCH_PROXY to null
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
        } to response.reasonComponent
    }

    override suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResult {
        check(server is StandaloneCloudServerImpl) { "Server must be a StandaloneCloudServerImpl" }

        if (connecting) {
            return ConnectionResultEnum.CONNECTION_IN_PROGRESS to null
        }

        connecting = true
        assert(connectionQueueCallback == null) { "Connection queue callback is not null" }
        connectionQueueCallback = CompletableDeferred<ConnectionResult>()

        val proxy = proxyServer
        if (proxy != null) {
            return switchServerOrQueueUnderSameProxy(proxy, server).also { connecting = false }
        }

        error("NOT SUPPORTED")
//        return switchServerOrQueueUnderNoProxy(server).also { connecting = false }
    }

    override suspend fun getLuckpermsMetaData(key: String): String? {
        return RequestLuckpermsMetaDataPacket(uuid, key).fireAndAwait(anyServer.connection)?.data
    }

    private suspend fun switchServerOrQueueUnderSameProxy(
        proxy: StandaloneProxyCloudServerImpl,
        target: StandaloneCloudServerImpl
    ): ConnectionResult {
        val underSameProxy =
            ClientboundIsServerManagedByThisProxyPacket(target.connection.virtualHost)
                .fireAndAwait(proxy.connection)
                ?.isManagedByThisProxy
                ?: return ConnectionResultEnum.CANNOT_COMMUNICATE_WITH_PROXY to null

        if (!underSameProxy) {
            return ConnectionResultEnum.CANNOT_SWITCH_PROXY to null
        }

        target.queue.addPlayerToQueue(this)
        return connectionQueueCallback!!.await()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("UnstableApiUsage", "DEPRECATION")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        send(ClientboundSendMessagePacket(uuid, message))
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
        part: TitlePart<T?>,
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

    override fun playSound(sound: Sound) {
        send(ClientboundPlaySoundPacket(uuid, sound))
    }

    override fun stopSound(stop: SoundStop) {
        send(ClientboundStopSoundPacket(uuid, stop))
    }

    override fun openBook(book: Book) {
        send(ClientboundOpenBookPacket(uuid, book))
    }

    override fun sendResourcePacks(request: ResourcePackRequest) { // TODO: Implement callback
        send(ClientboundSendResourcePacksPacket(uuid, request))
    }

    override fun removeResourcePacks(id: UUID, vararg others: UUID) {
        send(ClientboundRemoveResourcePacksPacket(uuid, id, *others))
    }

    override fun clearResourcePacks() {
        send(ClientboundClearResourcePacksPacket(uuid))
    }

    private fun send(packet: NettyPacket) {
        anyServer.connection.send(packet)
    }
}