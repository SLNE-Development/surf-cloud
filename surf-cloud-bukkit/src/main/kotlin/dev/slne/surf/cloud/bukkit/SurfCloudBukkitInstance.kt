package dev.slne.surf.cloud.bukkit

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.bukkit.listener.ListenerManager
import dev.slne.surf.cloud.bukkit.netty.BukkitNettyManager
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import java.nio.file.Path

@AutoService(SurfCloudInstance::class)
class SurfCloudBukkitInstance : SurfCloudCoreInstance(BukkitNettyManager) {
    override val dataFolder: Path
        get() = BukkitMain.instance.dataPath

    override val classLoader: ClassLoader
        get() = BukkitMain.instance.classLoader0

    override fun onEnable() {
        super.onEnable()

        ListenerManager.registerListeners()
    }

    override fun onDisable() {
        super.onDisable()

        ListenerManager.unregisterListeners()
    }
}

val bukkitCloudInstance
    get() = coreCloudInstance as SurfCloudBukkitInstance
