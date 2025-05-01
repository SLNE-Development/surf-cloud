package dev.slne.surf.cloud.bukkit.listener.punish.mute

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.core.common.messages.MessageManager
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerEvent
import java.util.*


abstract class AbstractMuteListener {
    protected fun <E> processEvent(event: E) where E : PlayerEvent, E : Cancellable {
        processEvent(event.player.uniqueId, event)
    }

    protected fun processEvent(playerUuid: UUID, event: Cancellable) {
        if (processMute(playerUuid)) {
            event.isCancelled = true
        }
    }


    protected fun processMute(playerUuid: UUID): Boolean {
        val player = playerUuid.toOfflineCloudPlayer()
        val longestActiveMute = player.punishmentManager.longestActiveMute() ?: return false
        MessageManager.Punish.Mute(longestActiveMute).sendMuteComponentToPunishedPlayer()

        return true
    }


    protected fun isMuted(playerUuid: UUID): Boolean {
        return CloudPlayerManager.getPlayer(playerUuid)?.punishmentManager?.isMuted == true
    }
}