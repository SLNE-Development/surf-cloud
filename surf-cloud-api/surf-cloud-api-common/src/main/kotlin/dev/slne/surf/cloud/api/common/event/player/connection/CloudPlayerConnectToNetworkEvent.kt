package dev.slne.surf.cloud.api.common.event.player.connection

import dev.slne.surf.cloud.api.common.event.player.CloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * Event that is called when a player connects to the network.
 */
class CloudPlayerConnectToNetworkEvent(source: Any, player: CloudPlayer) :
    CloudPlayerEvent(source, player)