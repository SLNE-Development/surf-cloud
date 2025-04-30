package dev.slne.surf.cloud.core.common.player

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.CloudPlayerPunishmentManager
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.core.common.player.punishment.CloudPlayerPunishmentManagerImpl
import java.util.*

abstract class CommonOfflineCloudPlayerImpl(override val uuid: UUID) : OfflineCloudPlayer {
    override val player: CloudPlayer?
        get() = this as? CloudPlayer ?: CloudPlayerManager.getPlayer(uuid)

    override suspend fun lastServer(): CloudServer? =
        lastServerRaw()?.let { CloudServerManager.retrieveServerByName(it) } as? CloudServer

    override suspend fun playedBefore(): Boolean {
        return player != null || lastSeen() != null
    }

    override val punishmentManager: CloudPlayerPunishmentManagerImpl
        get() = punishmentManagerCache.get(uuid)

    companion object {
        internal val punishmentManagerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .build<UUID, CloudPlayerPunishmentManagerImpl> { CloudPlayerPunishmentManagerImpl(it) }
    }
}