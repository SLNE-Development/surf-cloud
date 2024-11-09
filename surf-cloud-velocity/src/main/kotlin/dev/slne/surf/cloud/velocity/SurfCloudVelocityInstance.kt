package dev.slne.surf.cloud.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.SurfCloudInstance
import dev.slne.surf.cloud.core.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.coreCloudInstance
import java.nio.file.Path

@AutoService(SurfCloudInstance::class)
class SurfCloudVelocityInstance : SurfCloudCoreInstance(TODO()) {
    override val dataFolder: Path
        get() = TODO("Not yet implemented")

    override val classLoader: ClassLoader
        get() = TODO("Not yet implemented")
}

val velocityCloudInstance
    get() = coreCloudInstance as SurfCloudVelocityInstance
