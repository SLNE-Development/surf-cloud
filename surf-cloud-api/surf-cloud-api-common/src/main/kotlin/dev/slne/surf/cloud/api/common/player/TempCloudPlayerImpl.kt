package dev.slne.surf.cloud.api.common.player

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

class TempCloudPlayerImpl: CloudPlayer {

    @Deprecated("Deprecated in Java")
    @Suppress("UnstableApiUsage")
    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
    }

    override fun sendActionBar(message: Component) {

    }

    override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {

    }

    override fun showTitle(title: Title) {

    }

    override fun <T : Any?> sendTitlePart(part: TitlePart<T>, value: T & Any) {

    }

    override fun clearTitle() {

    }

    override fun resetTitle() {

    }

    override fun showBossBar(bar: BossBar) {

    }

    override fun hideBossBar(bar: BossBar) {

    }

    override fun playSound(sound: Sound) {

    }

    override fun playSound(sound: Sound, emitter: Emitter) {
        if (emitter != Emitter.self()) {
            throw UnsupportedOperationException("Only self emitters are supported")
        }
    }

    override fun playSound(sound: Sound, x: Double, y: Double, z: Double) {
    }

    override fun stopSound(stop: SoundStop) {

    }

    override fun openBook(book: Book) {

    }

    override fun sendResourcePacks(request: ResourcePackRequest) {

    }

    override fun removeResourcePacks(id: UUID, vararg others: UUID) {

    }

    override fun clearResourcePacks() {

    }
}