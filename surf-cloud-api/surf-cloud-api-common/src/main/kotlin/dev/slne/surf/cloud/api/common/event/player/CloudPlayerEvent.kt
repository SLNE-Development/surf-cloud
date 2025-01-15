package dev.slne.surf.cloud.api.common.event.player

import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * Base class for all events related to a specific player.
 *
 * @param source The object on which the event initially occurred.
 * @param player The player associated with the event.
 */
abstract class CloudPlayerEvent(source: Any, val player: CloudPlayer) : CloudEvent(source)