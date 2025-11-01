package dev.slne.surf.cloud.standalone.player.punishment.handler

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerUnpunishEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class DefaultMutePunishmentHandler {

    @CloudEventHandler
    fun onMute(event: CloudPlayerPunishEvent<PunishmentMute>) {
        val punishment = event.punishment
        if (!punishment.active) return

        PunishmentHandlerScope.launch {
            MessageManager.Punish.Mute(punishment)
                .sendMuteComponentToPunishedPlayer()
                .announceMute()
        }
    }

    @CloudEventHandler
    fun onUnmute(event: CloudPlayerUnpunishEvent<PunishmentMute>) {
        val punishment = event.punishment
        PunishmentHandlerScope.launch {
            MessageManager.Punish.Mute(punishment)
                .sendUnmuteComponentToPunishedPlayer()
                .announceUnmute()
        }
    }
}