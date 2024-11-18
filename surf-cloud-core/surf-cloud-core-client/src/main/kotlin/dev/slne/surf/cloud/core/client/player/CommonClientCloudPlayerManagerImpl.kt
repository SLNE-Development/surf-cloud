package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl

abstract class CommonClientCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
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