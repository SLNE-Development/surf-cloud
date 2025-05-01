package dev.slne.surf.cloud.api.common.event.offlineplayer

import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import java.io.Serial

/**
 * Base class for all events related to a specific player.
 *
 * @param source The object on which the event initially occurred.
 * @param player The player associated with the event.
 */
abstract class OfflineCloudPlayerEvent(source: Any, val player: OfflineCloudPlayer) : CloudEvent(source) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 2525857345685742731L
    }
}