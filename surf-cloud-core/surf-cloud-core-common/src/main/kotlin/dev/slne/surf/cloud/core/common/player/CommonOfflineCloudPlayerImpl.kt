package dev.slne.surf.cloud.core.common.player

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.core.common.coroutines.PunishmentCacheRefreshScope
import dev.slne.surf.cloud.core.common.player.punishment.CloudPlayerPunishmentManagerImpl
import dev.slne.surf.cloud.core.common.player.whitelist.CloudPlayerWhitelistManagerImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.minutes

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

    override val whitelistManager = CloudPlayerWhitelistManagerImpl(uuid)

    companion object {
        internal val punishmentManagerCache = Caffeine.newBuilder()
            .expireAfterAccess(10.minutes)
            .build<UUID, CloudPlayerPunishmentManagerImpl> { CloudPlayerPunishmentManagerImpl(it) }

        init {
            /**
             * Refreshes the punishment manager cache every 5 minutes to ensure that
             * the punishment managers for online players are up to date
             * and always available.
             */
            PunishmentCacheRefreshScope.launch {
                while (isActive) {
                    delay(5.minutes)
                    CloudPlayerManager.getOnlinePlayers().references.forEach { uuid ->
                        punishmentManagerCache.getIfPresent(uuid)
                    }
                }
            }
        }
    }
}