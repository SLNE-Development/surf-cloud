package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.serverManager
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import java.util.UUID

@AutoService(CloudPlayerManager::class)
class StandaloneCloudPlayerManagerImpl: CloudPlayerManagerImpl() {
    override fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ): CloudPlayer {
        return StandaloneCloudPlayerImpl(uuid).also {
            val server = serverManagerImpl.getServerById(serverUid)
            if (server != null) {
                it.servers.add(server)
            }
        }
    }
}