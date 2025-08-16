package dev.slne.surf.cloud.api.common.event.player.afk

import dev.slne.surf.cloud.api.common.event.player.CloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer

class AfkStateChangeEvent(val isAfk: Boolean, source: Any, player: CloudPlayer) :
    CloudPlayerEvent(source, player)