package dev.slne.surf.cloud.bukkit

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.bukkit.listener.ListenerManager
import dev.slne.surf.cloud.bukkit.netty.BukkitNettyManager
import dev.slne.surf.cloud.core.client.ClientCommonCloudInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader

@AutoService(SurfCloudInstance::class)
class SurfCloudBukkitInstance : ClientCommonCloudInstance(BukkitNettyManager) {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun onEnable() {
        super.onEnable()

        ListenerManager.registerListeners()
    }

    override suspend fun onDisable() {
        super.onDisable()

        ListenerManager.unregisterListeners()
    }
}

val bukkitCloudInstance get() = coreCloudInstance as SurfCloudBukkitInstance