package dev.slne.surf.cloud.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.core.client.ClientCommonCloudInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.velocity.listener.ListenerManager
import dev.slne.surf.cloud.velocity.netty.VelocityNettyManager

@AutoService(SurfCloudInstance::class)
class SurfCloudVelocityInstance : ClientCommonCloudInstance(VelocityNettyManager) {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun onEnable() {
        super.onEnable()

        ListenerManager.registerListeners()
    }

    override suspend fun onDisable() {
        super.onDisable()

        ListenerManager.unregisterListener()
    }
}

val velocityCloudInstance
    get() = coreCloudInstance as SurfCloudVelocityInstance
