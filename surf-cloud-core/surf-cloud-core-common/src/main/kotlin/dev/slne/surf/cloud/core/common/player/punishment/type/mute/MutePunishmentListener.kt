package dev.slne.surf.cloud.core.common.player.punishment.type.mute

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(PrePlayerJoinTask.MUTE_PUNISHMENT_LISTENER)
class MutePunishmentListener : PrePlayerJoinTask {

    @CloudEventHandler
    fun onPlayerMute(event: CloudPlayerPunishEvent<PunishmentMute>) {
        val mute = event.punishment as PunishmentMuteImpl
        if (mute.active) {
            mute.punishedPlayer().punishmentManager.cacheMute(mute)
        }
    }

    @CloudEventHandler
    fun onMuteUpdateEvent(event: CloudPlayerPunishmentUpdatedEvent<PunishmentMute>) {
        val mute = event.updatedPunishment as PunishmentMuteImpl
        mute.punishedPlayer().punishmentManager.updateCachedMute(mute)
    }

    override suspend fun preJoin(player: OfflineCloudPlayer): PrePlayerJoinTask.Result {
        require(player is CommonOfflineCloudPlayerImpl) { "Player must be an instance of CommonOfflineCloudPlayerImpl" }
        player.punishmentManager.cacheMutes()
        return PrePlayerJoinTask.Result.ALLOWED
    }
}