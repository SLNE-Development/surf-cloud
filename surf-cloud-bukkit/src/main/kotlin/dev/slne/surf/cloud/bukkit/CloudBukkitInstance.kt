package dev.slne.surf.cloud.bukkit

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.bukkit.listener.ListenerManager
import dev.slne.surf.cloud.bukkit.netty.BukkitNettyManager
import dev.slne.surf.cloud.bukkit.processor.BukkitListenerProcessor
import dev.slne.surf.cloud.core.client.ClientCommonCloudInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader

@AutoService(CloudInstance::class)
class CloudBukkitInstance : ClientCommonCloudInstance(BukkitNettyManager) {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun onEnable() {
        super.onEnable()

        bean<BukkitListenerProcessor>().registerListeners()
        ListenerManager.registerListeners()
    }

    override suspend fun onDisable() {
        super.onDisable()

        ListenerManager.unregisterListeners()
    }
}

val bukkitCloudInstance get() = coreCloudInstance as CloudBukkitInstance