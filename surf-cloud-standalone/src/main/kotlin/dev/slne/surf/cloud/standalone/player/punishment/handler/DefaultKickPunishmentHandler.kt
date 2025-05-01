package dev.slne.surf.cloud.standalone.player.punishment.handler

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DefaultKickPunishmentHandler {

    @CloudEventHandler
    fun onKick(event: CloudPlayerPunishEvent<PunishmentKick>) {
        val punishment = event.punishment
        val punishedPlayer = punishment.punishedPlayer()
        val messages = MessageManager.Punish.Kick(punishment)

        punishedPlayer.player?.disconnect(messages.kickDisconnectComponent())
        PunishmentHandlerScope.launch { messages.announceKick() }
    }
}