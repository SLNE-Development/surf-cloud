package dev.slne.surf.cloud.core.common.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import java.util.*

@AutoService(CloudPlayerManager::class)
class CloudPlayerManagerImpl : CloudPlayerManager {
    private val players = mutableObject2ObjectMapOf<UUID, CloudPlayer>().synchronize()

    init {
        checkInstantiationByServiceLoader()
    }

    override fun getPlayer(uuid: UUID?): CloudPlayer? {
        return players[uuid]
    }

    fun addPlayer(player: CloudPlayer) {
        players[player.uuid] = player
    }

    fun removePlayer(player: CloudPlayer) {
        players.remove(player.uuid)
    }
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl