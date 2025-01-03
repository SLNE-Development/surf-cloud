package dev.slne.surf.cloud.velocity.listener

import dev.slne.surf.cloud.velocity.plugin
import dev.slne.surf.cloud.velocity.processor.VelocityListenerProcessor
import dev.slne.surf.cloud.velocity.proxy

object ListenerManager {

    fun registerListeners() {
//        register(ConnectionListener)

        VelocityListenerProcessor.registerListeners()
    }

    fun unregisterListener() {
        proxy.eventManager.unregisterListeners(plugin)
    }

    private fun register(listener: Any) {
        proxy.eventManager.register(plugin, listener)
    }
}