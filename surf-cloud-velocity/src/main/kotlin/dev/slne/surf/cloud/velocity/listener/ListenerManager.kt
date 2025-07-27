package dev.slne.surf.cloud.velocity.listener

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.velocity.plugin
import dev.slne.surf.cloud.velocity.processor.VelocityListenerProcessor
import dev.slne.surf.cloud.velocity.proxy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class ListenerManager : CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Register Velocity listeners") {
            registerListeners()
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Unregister Velocity listeners") {
            unregisterListener()
        }
    }

    fun registerListeners() {
        VelocityListenerProcessor.registerListeners()
    }

    fun unregisterListener() {
        proxy.eventManager.unregisterListeners(plugin)
    }

    private fun register(listener: Any) {
        proxy.eventManager.register(plugin, listener)
    }
}