package dev.slne.surf.cloud.bukkit.listener

import dev.slne.surf.cloud.bukkit.listener.exception.SurfFatalErrorExceptionListener
import dev.slne.surf.cloud.bukkit.listener.player.ConnectionListener
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.event.HandlerList

object ListenerManager {

    fun registerListeners() {
        ConnectionListener.register()
        SurfFatalErrorExceptionListener.register()
    }

    fun unregisterListeners() {
        HandlerList.unregisterAll(plugin)
    }
}