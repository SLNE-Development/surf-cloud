package dev.slne.surf.cloud.bukkit.netty

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.CloudClientNettyManager
import dev.slne.surf.cloud.core.client.netty.CommonCloudClientNettyManagerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader

@AutoService(CloudClientNettyManager::class)
class BukkitCloudClientNettyManagerImpl : CommonCloudClientNettyManagerImpl() {
    init {
        checkInstantiationByServiceLoader()
    }

    override val client get() = bean<BukkitNettyManager>().nettyClient
}