package dev.slne.surf.cloud.bukkit.processor

import dev.slne.surf.cloud.api.lifecycle.SurfLifecycle
import dev.slne.surf.cloud.core.processors.AbstractLifecycleProcessor
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.stereotype.Component

@Component
class BukkitLifecycleProcessor : AbstractLifecycleProcessor() {
    override fun getProvidingClass(lifecycle: SurfLifecycle) =
        JavaPlugin.getProvidingPlugin(lifecycle.javaClass).javaClass.kotlin
}
