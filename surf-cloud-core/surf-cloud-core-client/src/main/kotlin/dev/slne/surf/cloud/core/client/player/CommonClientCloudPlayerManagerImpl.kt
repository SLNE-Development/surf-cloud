package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import net.kyori.adventure.audience.Audience
import java.util.*

abstract class CommonClientCloudPlayerManagerImpl<Platform : Audience, P : ClientCloudPlayerImpl<Platform>> :
    CloudPlayerManagerImpl<P>() {
    override suspend fun updateProxyServer(
        player: P,
        serverName: String
    ) {
        player.proxyServerName = serverName
    }

    override suspend fun updateServer(
        player: P,
        serverName: String
    ) {
        player.serverName = serverName
    }

    override suspend fun removeProxyServer(
        player: P,
        serverName: String
    ) {
        player.proxyServerName = null
    }

    override suspend fun removeServer(
        player: P,
        serverName: String
    ) {
        player.serverName = null
    }

    override fun getProxyServerName(player: P): String? {
        return player.proxyServerName
    }

    override fun getServerName(player: P): String? {
        return player.serverName
    }

    override fun getOfflinePlayer(uuid: UUID, createIfNotExists: Boolean): CommonOfflineCloudPlayerImpl {
        return OfflineCloudPlayerImpl(uuid, createIfNotExists)
    }

    abstract fun getAudience(uuid: UUID): Audience?
}

val commonPlayerManagerImpl get() = playerManagerImpl as CommonClientCloudPlayerManagerImpl<*, *>