package dev.slne.surf.cloud.api.common.event.player.connection

import dev.slne.surf.cloud.api.common.event.player.CloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import java.io.Serial

class CloudPlayerDisconnectFromNetworkEvent(source: Any, player: CloudPlayer) :
    CloudPlayerEvent(source, player) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1730514074081406974L
    }
}