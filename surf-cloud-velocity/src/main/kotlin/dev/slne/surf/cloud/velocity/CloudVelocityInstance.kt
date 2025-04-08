package dev.slne.surf.cloud.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.core.client.ClientCommonCloudInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.velocity.listener.ListenerManager
import dev.slne.surf.cloud.velocity.netty.VelocityNettyManager

@AutoService(CloudInstance::class)
class CloudVelocityInstance : ClientCommonCloudInstance(VelocityNettyManager) {
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
    get() = coreCloudInstance as CloudVelocityInstance
