package dev.slne.surf.cloud.velocity.listener.player

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class PlayerKickListener {

    @Subscribe
    fun onPlayerKickedFromServer(event: KickedFromServerEvent) {
        val reason = event.serverKickReason.getOrNull() ?: return
        val plainReason = PlainTextComponentSerializer.plainText().serialize(reason)

        println("On kick with plain reason: $plainReason")
        if (reason.contains(CommonComponents.DISCONNECT_HEADER)) { // TODO: 21.04.2025 20:34 - test
            println("contains disconnect header")
            event.result = KickedFromServerEvent.DisconnectPlayer.create(reason)
        } else {
            println("NOT contains disconnect header")
        }
    }
}