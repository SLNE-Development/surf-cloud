package dev.slne.surf.cloud.bukkit.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import java.util.*

@AutoService(CloudPlayerManager::class)
class BukkitCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
    override fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ) = ClientCloudPlayerImpl(uuid).also {
        if (proxy) {
            it.proxyServerUid = serverUid
        } else {
            it.serverUid = serverUid
        }
    }

    override fun updateProxyServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).proxyServerUid = serverUid
    }

    override fun updateServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).serverUid = serverUid
    }

    override fun removeProxyServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).proxyServerUid = null
    }

    override fun removeServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).serverUid = null
    }
}