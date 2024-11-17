package dev.slne.surf.cloud.bukkit.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import java.util.*

@AutoService(CloudPlayerManager::class)
class BukkitCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
    override fun createPlayer(uuid: UUID): CloudPlayer {
        return ClientCloudPlayerImpl(uuid).also { it.audience = server.getPlayer(uuid) }
    }
}