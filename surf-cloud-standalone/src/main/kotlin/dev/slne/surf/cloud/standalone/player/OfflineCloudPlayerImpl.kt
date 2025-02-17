package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.player.playerManager
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.player.db.service.CloudPlayerService
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import net.kyori.adventure.text.Component
import java.net.InetAddress
import java.time.ZonedDateTime
import java.util.*

class OfflineCloudPlayerImpl(override val uuid: UUID) : OfflineCloudPlayer {
    companion object {
        private val service by lazy { bean<CloudPlayerService>() }
    }

    override suspend fun nameHistory(): NameHistory {
        TODO("Not yet implemented")
    }

    override suspend fun lastServerRaw(): String? =
        player?.lastServerRaw() ?: service.findLastServer(uuid)

    override suspend fun lastServer(): CloudServer? = player?.lastServer()
        ?: lastServerRaw()?.let { serverManagerImpl.retrieveServerByName(it) } as? CloudServer

    override suspend fun lastSeen(): ZonedDateTime? =
        player?.lastSeen() ?: service.findLastSeen(uuid)

    override suspend fun latestIpAddress(): InetAddress? =
        player?.latestIpAddress() ?: service.findLastIpAddress(uuid)

    override suspend fun displayName(): Component? {
        return player?.displayName() ?: serverManagerImpl.requestOfflineDisplayName(uuid)
    }

    override val player: CloudPlayer? get() = playerManager.getPlayer(uuid)

    override suspend fun <R> getLuckpermsMetaData(
        key: String,
        transformer: (String) -> R
    ): R? {
        TODO("Not yet implemented")
    }

    override suspend fun getLuckpermsMetaData(key: String): String? {
        TODO("Not yet implemented")
    }
}