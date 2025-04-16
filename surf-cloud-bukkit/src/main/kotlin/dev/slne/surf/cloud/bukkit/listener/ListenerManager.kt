package dev.slne.surf.cloud.bukkit.listener

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.bukkit.listener.exception.SurfFatalErrorExceptionListener
import dev.slne.surf.cloud.bukkit.listener.player.ConnectionListener
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.nmsBridge
import org.bukkit.event.HandlerList
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@OptIn(NmsUseWithCaution::class)
@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class ListenerManager: CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Registering listeners") {
            registerListeners()
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Unregistering listeners") {
            unregisterListeners()
        }
    }

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