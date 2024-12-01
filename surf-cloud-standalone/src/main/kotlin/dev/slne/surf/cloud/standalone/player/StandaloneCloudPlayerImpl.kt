package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*

class StandaloneCloudPlayerImpl(uuid: UUID) : CommonCloudPlayerImpl(uuid) {
    @Volatile
    var proxyServer: StandaloneProxyCloudServerImpl? = null

    @Volatile
    var server: StandaloneCloudServerImpl? = null

    override val connectedToProxy get() = proxyServer != null
    override val connectedToServer get() = server != null
    private val anyServer: ServerCommonCloudServer
        get() = server ?: proxyServer ?: error("Player is not connected to a server")

    override suspend fun displayName(): Component = ClientboundRequestDisplayNamePacket(uuid)
        .fireAndAwaitUrgent(anyServer.connection)?.displayName
        ?: error("Failed to get display name (probably timed out)")

    override suspend fun connectToServer(server: CommonCloudServer): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServerOrQueue(server: CommonCloudServer): ConnectionResult {
        TODO("Not yet implemented")
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