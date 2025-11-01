package dev.slne.surf.cloud.bukkit.listener.punish.mute

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.springframework.stereotype.Component

@Component
class PaperMuteListener : AbstractMuteListener(), Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onAsyncChat(event: AsyncChatEvent) {
        processEvent(event)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSignChange(event: SignChangeEvent) {
        processEvent(event.player.uniqueId, event)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerEditBook(event: PlayerEditBookEvent) {
        processEvent(event)
    }
}