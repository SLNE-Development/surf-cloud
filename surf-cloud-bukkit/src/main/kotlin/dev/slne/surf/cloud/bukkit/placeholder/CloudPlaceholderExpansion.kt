package dev.slne.surf.cloud.bukkit.placeholder

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.bukkit.placeholder.afk.CloudAfkPlaceholder
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiExpansion
import dev.slne.surf.surfapi.bukkit.api.hook.papi.papiHook
import dev.slne.surf.surfapi.core.api.util.objectListOf
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

object CloudPlaceholderExpansion : PapiExpansion(
    "cloud",
    objectListOf(CloudAfkPlaceholder)
)

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class CloudPlaceholderExpansionLifecycle : CloudLifecycleAware {
    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Registering placeholders") {
            papiHook.register(CloudPlaceholderExpansion)
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Unregistering placeholders") {
            papiHook.unregister(CloudPlaceholderExpansion)
        }
    }
}