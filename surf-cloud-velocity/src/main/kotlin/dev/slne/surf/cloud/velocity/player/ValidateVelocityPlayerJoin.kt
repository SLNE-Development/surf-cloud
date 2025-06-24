package dev.slne.surf.cloud.velocity.player

import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.velocity.proxy
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Order(PrePlayerJoinTask.VELOCITY_PLAYER_JOIN_VALIDATION)
class ValidateVelocityPlayerJoin : PrePlayerJoinTask {
    override suspend fun preJoin(player: OfflineCloudPlayer): PrePlayerJoinTask.Result {
        return if (proxy.allServers.isEmpty()) {
            PrePlayerJoinTask.Result.DENIED(
                MessageManager.noServersAvailableToJoin
            )
        } else {
            PrePlayerJoinTask.Result.ALLOWED
        }
    }
}