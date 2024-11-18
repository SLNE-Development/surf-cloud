package dev.slne.surf.cloud.bukkit.netty

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.CloudClientNettyManager
import dev.slne.surf.cloud.core.client.netty.CommonCloudClientNettyManagerImpl

@AutoService(CloudClientNettyManager::class)
class BukkitCloudClientNettyManagerImpl : CommonCloudClientNettyManagerImpl() {
    override val client get() = BukkitNettyManager.nettyClient
}