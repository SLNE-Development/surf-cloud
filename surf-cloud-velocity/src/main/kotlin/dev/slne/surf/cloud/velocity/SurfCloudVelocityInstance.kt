package dev.slne.surf.cloud.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.velocity.netty.VelocityNettyManager
import java.nio.file.Path

@AutoService(SurfCloudInstance::class)
class SurfCloudVelocityInstance : SurfCloudCoreInstance(VelocityNettyManager) {
    override val dataFolder: Path get() = VelocityMain.instance.dataPath
    override val classLoader: ClassLoader get() = VelocityMain.instance.javaClass.classLoader
}

val velocityCloudInstance
    get() = coreCloudInstance as SurfCloudVelocityInstance
