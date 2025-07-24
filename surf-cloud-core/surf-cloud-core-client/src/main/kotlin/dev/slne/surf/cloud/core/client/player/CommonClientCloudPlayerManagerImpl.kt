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
        serverUid: Long
    ) {
        player.proxyServerUid = serverUid
    }

    override suspend fun updateServer(
        player: P,
        serverUid: Long
    ) {
        player.serverUid = serverUid
    }

    override suspend fun removeProxyServer(
        player: P,
        serverUid: Long
    ) {
        player.proxyServerUid = null
    }

    override suspend fun removeServer(
        player: P,
        serverUid: Long
    ) {
        player.serverUid = null
    }

    override fun getProxyServerUid(player: P): Long? {
        return player.proxyServerUid
    }

    override fun getServerUid(player: P): Long? {
        return player.serverUid
    }

    override fun getOfflinePlayer(uuid: UUID, createIfNotExists: Boolean): CommonOfflineCloudPlayerImpl {
        return OfflineCloudPlayerImpl(uuid, createIfNotExists)
    }

    abstract fun getAudience(uuid: UUID): Audience?
}

val commonPlayerManagerImpl get() = playerManagerImpl as CommonClientCloudPlayerManagerImpl<*, *>