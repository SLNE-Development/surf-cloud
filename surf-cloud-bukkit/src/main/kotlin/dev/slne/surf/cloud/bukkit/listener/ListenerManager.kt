package dev.slne.surf.cloud.bukkit.listener

import dev.slne.surf.cloud.bukkit.listener.exception.SurfFatalErrorExceptionListener
import dev.slne.surf.cloud.bukkit.listener.player.ConnectionListener
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.nmsBridge
import org.bukkit.event.HandlerList

@OptIn(NmsUseWithCaution::class)
object ListenerManager {

    fun registerListeners() {
        ConnectionListener.register()
        SurfFatalErrorExceptionListener.register()
        nmsBridge.registerClientboundPacketListener(SilentDisconnectListener)
    }

    fun unregisterListeners() {
        HandlerList.unregisterAll(plugin)
        nmsBridge.unregisterClientboundPacketListener(SilentDisconnectListener)
    }
}