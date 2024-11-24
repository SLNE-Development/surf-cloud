package dev.slne.surf.cloud.bukkit.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.CommonClientCloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import java.util.*

@AutoService(CloudPlayerManager::class)
class BukkitCloudPlayerManagerImpl : CommonClientCloudPlayerManagerImpl() {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun createPlayer(
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

    override fun getAudience(uuid: UUID): Audience? = Bukkit.getPlayer(uuid)
}