package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import net.kyori.adventure.audience.Audience
import java.util.UUID

abstract class CommonClientCloudPlayerManagerImpl : CloudPlayerManagerImpl() {
    override suspend fun updateProxyServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).proxyServerUid = serverUid
    }

    override suspend fun updateServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).serverUid = serverUid
    }

    override suspend fun removeProxyServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).proxyServerUid = null
    }

    override suspend fun removeServer(
        player: CloudPlayer,
        serverUid: Long
    ) {
        (player as ClientCloudPlayerImpl).serverUid = null
    }

    abstract fun getAudience(uuid: UUID): Audience?
}
val commonPlayerManagerImpl get() = playerManagerImpl as CommonClientCloudPlayerManagerImpl