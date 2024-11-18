package dev.slne.surf.cloud.bukkit.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.CommonClientCloudPlayerManagerImpl
import java.util.*

@AutoService(CloudPlayerManager::class)
class BukkitCloudPlayerManagerImpl : CommonClientCloudPlayerManagerImpl() {
    override fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ) = BukkitClientCloudPlayerImpl(uuid).also {
        if (proxy) {
            it.proxyServerUid = serverUid
        } else {
            it.serverUid = serverUid
        }
    }
}