package dev.slne.surf.cloud.api.common.event.player.connection

import dev.slne.surf.cloud.api.common.event.player.CloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * Event triggered when a player connects to the network.
 *
 * @param source The object on which the event initially occurred.
 * @param player The player who connected to the network.
 */
class CloudPlayerConnectToNetworkEvent(source: Any, player: CloudPlayer) :
    CloudPlayerEvent(source, player)