package dev.slne.surf.cloud.bukkit

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.bukkit.command.PaperCommandManager
import dev.slne.surf.cloud.bukkit.listener.ListenerManager
import dev.slne.surf.cloud.bukkit.netty.BukkitNettyManager
import dev.slne.surf.cloud.bukkit.placeholder.CloudPlaceholderExpansion
import dev.slne.surf.cloud.bukkit.processor.BukkitListenerProcessor
import dev.slne.surf.cloud.core.client.ClientCommonCloudInstance
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.surfapi.bukkit.api.hook.papi.papiHook

@AutoService(CloudInstance::class)
class CloudBukkitInstance : ClientCommonCloudInstance(BukkitNettyManager) {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun onEnable() {
        super.onEnable()

        PaperCommandManager.registerCommands()
        bean<BukkitListenerProcessor>().registerListeners()
        ListenerManager.registerListeners()

        papiHook.register(CloudPlaceholderExpansion)
    }

    override suspend fun onDisable() {
        super.onDisable()

        ListenerManager.unregisterListeners()
        papiHook.unregister(CloudPlaceholderExpansion)
    }
}

val bukkitCloudInstance get() = coreCloudInstance as CloudBukkitInstance