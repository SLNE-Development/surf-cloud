package dev.slne.surf.cloud.standalone.player.whitelist

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistSettings
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.coroutines.CommonScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.player.whitelist.AbstractWhitelistSettings
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.standalone.config.ConfigReloadAware
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component

@Component
class WhitelistSettingsImpl(
    private val service: WhitelistServiceImpl,
) : AbstractWhitelistSettings(), ConfigReloadAware,
    CloudLifecycleAware { // TODO: 06.08.2025 21:02 - config reload
    private val refreshMutex = Mutex()

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Loading WhitelistSettings") {
            val whitelistConfig = standaloneConfig.whitelist
            enforcedServers.addAll(whitelistConfig.enforcedServers.map { it.lowercase() })
            enforcedGroups.addAll(whitelistConfig.enforcedGroups.map { it.lowercase() })

            enforcedServers.subscribe { added, element ->
                if (added) {
                    whitelistConfig.enforcedServers.add(element)
//                WhitelistConfig.save()
                } else {
                    whitelistConfig.enforcedServers.remove(element)
//                WhitelistConfig.save()
                }
            }

            enforcedGroups.subscribe { added, element ->
                if (added) {
                    whitelistConfig.enforcedGroups.add(element)
//                WhitelistConfig.save()
                } else {
                    whitelistConfig.enforcedGroups.remove(element)
//                WhitelistConfig.save()
                }
            }
        }
    }

    /**
     * Refreshes the whitelist-enforcement state for all online players
     * and disconnects anyone that is not permitted to stay.
     *
     * Steps
     * 1. Stream every online player as a Flow.
     * 2. Ask the service for each player’s whitelist status (batched SQL call).
     * 3. Keep only players on servers where whitelist enforcement is active.
     * 4. Map the status to a disconnect message (if any) and kick.
     *
     * Executed inside a `refreshMutex` so that no two refresh runs overlap.
     */
    override fun refresh() {
        CommonScope.launch {
            refreshMutex.withLock {
                CloudPlayerManager.getOnlinePlayers()
                    .asFlow()
                    .filter { it.connectedToServer } // fast filter
                    .map { player ->
                        val server = player.currentServer()
                        player.uuid to (server.name to server.group) // prepare service input
                    }
                    .let { service.whitelistStatusFor(it) } // Flow<Pair<UUID, WhitelistStatus>>
                    .mapNotNull { (uuid, status) ->
                        val player = uuid.toCloudPlayer() ?: return@mapNotNull null
                        if (!player.connectedToServer) return@mapNotNull null
                        player.takeIf {
                            WhitelistSettings.isWhitelistEnforcedFor(it.currentServer())
                        }?.let { it to status }
                    }
                    .mapNotNull { (player, status) ->
                        when (status) {
                            WhitelistStatus.NONE -> MessageManager.Whitelist.notWhitelistedDisconnectHeader
                            WhitelistStatus.BLOCKED -> MessageManager.Whitelist.blockedWhitelistDisconnectHeader
                            WhitelistStatus.UNKNOWN -> MessageManager.Whitelist.errorWhileVerifyingWhitelist
                            WhitelistStatus.ACTIVE -> null
                        }?.let { player to it }
                    }
                    .collect { (player, disconnectMessage) -> player.disconnect(disconnectMessage) }
            }
        }
    }
}