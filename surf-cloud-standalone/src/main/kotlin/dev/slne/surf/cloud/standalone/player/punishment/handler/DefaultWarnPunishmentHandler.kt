package dev.slne.surf.cloud.standalone.player.punishment.handler

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class DefaultWarnPunishmentHandler {
    @CloudEventHandler
    fun onWarn(event: CloudPlayerPunishEvent<PunishmentWarn>) {
        PunishmentHandlerScope.launch {
            MessageManager.Punish.Warn(event.punishment)
                .warnPunishedPlayer()
                .announceWarn()
        }
    }
}