package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
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
    var proxyServer: ServerCloudServer? = null

    @Volatile
    var server: ServerCloudServer? = null

    override val connectedToProxy get() = proxyServer != null
    override val connectedToServer get() = server != null
    private val anyServer
        get() = server ?: proxyServer ?: error("Player is not connected to a server")

    @Deprecated("Deprecated in Java")
    @Suppress("UnstableApiUsage")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        anyServer.connection.send(ClientboundSendMessagePacket(uuid, message))
    }

    override fun sendActionBar(message: Component) {
        anyServer.connection.send(ClientboundSendActionBarPacket(uuid, message))
    }

    override fun sendPlayerListHeaderAndFooter(
        header: Component,
        footer: Component
    ) {
        anyServer.connection.send(
            ClientboundSendPlayerListHeaderAndFooterPacket(
                uuid,
                header,
                footer
            )
        )
    }

    override fun showTitle(title: Title) {
        anyServer.connection.send(ClientboundShowTitlePacket(uuid, title))
    }

    override fun <T : Any> sendTitlePart(
        part: TitlePart<T?>,
        value: T
    ) {
        anyServer.connection.send(ClientboundSendTitlePartPacket(uuid, part, value))
    }

    override fun clearTitle() {
        anyServer.connection.send(ClientboundClearTitlePacket(uuid))
    }

    override fun resetTitle() {
        anyServer.connection.send(ClientboundResetTitlePacket(uuid))
    }

    override fun showBossBar(bar: BossBar) {
        anyServer.connection.send(ClientboundShowBossBarPacket(uuid, bar))
    }

    override fun hideBossBar(bar: BossBar) {
        anyServer.connection.send(ClientboundHideBossBarPacket(uuid, bar))
    }

    override fun playSound(
        sound: Sound,
        x: Double,
        y: Double,
        z: Double
    ) {
        anyServer.connection.send(ClientboundPlaySoundPacket(uuid, sound, x, y, z))
    }

    override fun playSound(
        sound: Sound,
        emitter: Emitter
    ) {
        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }

        anyServer.connection.send(ClientboundPlaySoundPacket(uuid, sound, emitter))
    }

    override fun playSound(sound: Sound) {
        anyServer.connection.send(ClientboundPlaySoundPacket(uuid, sound))
    }

    override fun stopSound(stop: SoundStop) {
        anyServer.connection.send(ClientboundStopSoundPacket(uuid, stop))
    }

    override fun openBook(book: Book) {
        anyServer.connection.send(ClientboundOpenBookPacket(uuid, book))
    }

    override fun sendResourcePacks(request: ResourcePackRequest) {
        anyServer.connection.send(ClientboundSendResourcePacksPacket(uuid, request))
    }

    override fun removeResourcePacks(id: UUID, vararg others: UUID) {
        anyServer.connection.send(ClientboundRemoveResourcePacksPacket(uuid, id, *others))
    }

    override fun clearResourcePacks() {
        anyServer.connection.send(ClientboundClearResourcePacks(uuid))
    }
}