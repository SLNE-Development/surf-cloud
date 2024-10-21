package dev.slne.surf.cloud.bukkit

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.SurfCloudInstance
import dev.slne.surf.cloud.core.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.coreCloudInstance
import java.nio.file.Path

@AutoService(SurfCloudInstance::class)
class SurfCloudBukkitInstance : SurfCloudCoreInstance() {
    override val dataFolder: Path
        get() = BukkitMain.instance.dataPath

    override val classLoader: ClassLoader
        get() = BukkitMain.instance.classLoader0
}

val bukkitCloudInstance
    get() = coreCloudInstance as SurfCloudBukkitInstance
