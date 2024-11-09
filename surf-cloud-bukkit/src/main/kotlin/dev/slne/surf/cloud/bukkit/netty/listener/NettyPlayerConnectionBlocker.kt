package dev.slne.surf.cloud.bukkit.netty.listener

import dev.slne.surf.cloud.core.messages.MessageManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

object NettyPlayerConnectionBlocker : Listener {
    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageManager.serverStarting)
    }
}