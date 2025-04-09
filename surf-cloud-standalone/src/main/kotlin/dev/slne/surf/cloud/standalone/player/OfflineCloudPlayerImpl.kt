package dev.slne.surf.cloud.standalone.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerService
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*

class OfflineCloudPlayerImpl(uuid: UUID) : CommonOfflineCloudPlayerImpl(uuid) {
    companion object {
        private val service by lazy { bean<CloudPlayerService>() }
    }

    override suspend fun nameHistory(): NameHistory {
        val player = player
        if (player != null) {
            return player.nameHistory()
        }

        return service.findNameHistories(uuid)
    }

    override suspend fun lastServerRaw(): String? =
        player?.lastServerRaw() ?: service.findLastServer(uuid)

    override suspend fun lastServer(): CloudServer? = player?.lastServer()
        ?: lastServerRaw()?.let { serverManagerImpl.retrieveServerByName(it) } as? CloudServer

    override suspend fun lastSeen(): ZonedDateTime? =
        player?.lastSeen() ?: service.findLastSeen(uuid)

    override suspend fun firstSeen(): ZonedDateTime? =
        player?.firstSeen() ?: service.findFirstSeen(uuid)

    override suspend fun latestIpAddress(): Inet4Address? =
        player?.latestIpAddress() ?: service.findLastIpAddress(uuid)

    override suspend fun displayName(): Component? {
        return player?.displayName() ?: serverManagerImpl.requestOfflineDisplayName(uuid)
    }

    override suspend fun name(): String? {
        return player?.name() ?: PlayerLookupService.getUsername(uuid)
    }

    override val player: CloudPlayer? get() = CloudPlayerManager.getPlayer(uuid)

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