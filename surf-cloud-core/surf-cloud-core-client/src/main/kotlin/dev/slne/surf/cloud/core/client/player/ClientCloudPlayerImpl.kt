package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwait
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_URGENT_TIMEOUT
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
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
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*

abstract class ClientCloudPlayerImpl(uuid: UUID) : CommonCloudPlayerImpl(uuid) {
    @Volatile
    var proxyServerUid: Long? = null

    @Volatile
    var serverUid: Long? = null

    override val connectedToProxy get() = proxyServerUid != null
    override val connectedToServer get() = serverUid != null

    /**
     * The audience for this player. If the player is on this server, this will point to
     * the bukkit / velocity player. Otherwise packets will be sent to the player via the network.
     */
    protected abstract val audience: Audience?

    override suspend fun displayName(): Component {
        val localName = audience?.pointers()?.get(Identity.DISPLAY_NAME)?.orElse(null)
        if (localName != null) {
            return localName
        }

        return ServerboundRequestDisplayNamePacket(uuid).fireAndAwait(DEFAULT_URGENT_TIMEOUT)?.displayName
            ?: error("Failed to get display name (probably timed out)")
    }

    override suspend fun connectToServer(server: CloudServer): ConnectionResultEnum {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResultEnum {
        TODO("Not yet implemented")
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
}