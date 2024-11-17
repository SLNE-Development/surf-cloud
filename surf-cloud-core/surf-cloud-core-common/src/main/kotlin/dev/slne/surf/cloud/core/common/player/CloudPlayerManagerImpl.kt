package dev.slne.surf.cloud.core.common.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import java.util.*

abstract class CloudPlayerManagerImpl : CloudPlayerManager {
    private val players = mutableObject2ObjectMapOf<UUID, CloudPlayer>().synchronize()

    init {
        checkInstantiationByServiceLoader()
    }

    override fun getPlayer(uuid: UUID?): CloudPlayer? {
        return players[uuid]
    }

    abstract fun createPlayer(uuid: UUID, serverUid: Long, proxy: Boolean): CloudPlayer

    fun addPlayer(player: CloudPlayer) {
        players[player.uuid] = player
    }

    fun addPlayer(uuid: UUID, serverUid: Long, proxy: Boolean) {
        addPlayer(createPlayer(uuid, serverUid, proxy))
    }

    fun removePlayer(player: CloudPlayer) {
        players.remove(player.uuid)
    }

    fun removePlayer(uuid: UUID) {
        players.remove(uuid)
    }
}

val playerManagerImpl get() = CloudPlayerManager.instance as CloudPlayerManagerImpl