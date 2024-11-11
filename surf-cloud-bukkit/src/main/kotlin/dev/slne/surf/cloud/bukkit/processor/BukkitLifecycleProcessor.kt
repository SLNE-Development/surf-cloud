package dev.slne.surf.cloud.bukkit.processor

import dev.slne.surf.cloud.api.common.lifecycle.SurfLifecycle
import dev.slne.surf.cloud.core.common.processors.AbstractLifecycleProcessor
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.stereotype.Component

@Component
class BukkitLifecycleProcessor : AbstractLifecycleProcessor() {
    override fun getProvidingClass(lifecycle: SurfLifecycle) =
        JavaPlugin.getProvidingPlugin(lifecycle.javaClass).javaClass.kotlin
}
