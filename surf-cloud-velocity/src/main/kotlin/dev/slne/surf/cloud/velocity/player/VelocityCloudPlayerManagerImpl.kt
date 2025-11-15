package dev.slne.surf.cloud.velocity.player

import com.google.auto.service.AutoService
import com.velocitypowered.api.proxy.Player
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.CommonClientCloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.velocity.proxy
import java.net.Inet4Address
import java.util.*

@AutoService(CloudPlayerManager::class)
class VelocityCloudPlayerManagerImpl :
    CommonClientCloudPlayerManagerImpl<Player, VelocityClientCloudPlayerImpl>() {
    init {
        checkInstantiationByServiceLoader()
    }

    override fun createPlayer(
        uuid: UUID,
        name: String,
        proxy: Boolean,
        ip: Inet4Address,
        serverName: String
    ): VelocityClientCloudPlayerImpl {
        return VelocityClientCloudPlayerImpl(uuid, name).also {
            if (proxy) {
                it.proxyServerName = serverName
            } else {
                it.serverName = serverName
            }
        }
    }

    override fun getAudience(uuid: UUID): Player? = proxy.getPlayer(uuid).orElse(null)
}