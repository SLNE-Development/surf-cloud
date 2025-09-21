package dev.slne.surf.cloud.bukkit.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.CommonClientCloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.net.Inet4Address
import java.util.*

@AutoService(CloudPlayerManager::class)
class BukkitCloudPlayerManagerImpl : CommonClientCloudPlayerManagerImpl<Player, BukkitClientCloudPlayerImpl>() {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun createPlayer(
        uuid: UUID,
        name: String,
        proxy: Boolean,
        ip: Inet4Address,
        serverName: String
    ) = BukkitClientCloudPlayerImpl(uuid, name).also {
        if (proxy) {
            it.proxyServerName = serverName
        } else {
            it.serverName = serverName
        }
    }

    override fun getAudience(uuid: UUID): Audience? = Bukkit.getPlayer(uuid)
}