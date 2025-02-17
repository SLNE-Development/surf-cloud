package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.playerManager
import java.util.*

abstract class CommonOfflineCloudPlayerImpl(override val uuid: UUID) : OfflineCloudPlayer {
    override val player: CloudPlayer?
        get() = this as? CloudPlayer ?: playerManager.getPlayer(uuid)
}