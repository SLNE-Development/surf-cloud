package dev.slne.surf.cloud.api.common.event.player

import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer

abstract class CloudPlayerEvent(source: Any, val player: CloudPlayer) : CloudEvent(source)