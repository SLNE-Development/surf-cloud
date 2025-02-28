package dev.slne.surf.cloud.standalone.player.db.service

import dev.slne.surf.cloud.core.common.coroutines.PlayerDatabaseScope
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.db.player.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.player.name.CloudPlayerNameHistoryEntity
import dev.slne.surf.cloud.standalone.player.db.repository.CloudPlayerRepository
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*


@Service
@Transactional
class CloudPlayerService(
    private val cloudPlayerRepository: CloudPlayerRepository
) {

    suspend fun findLastServer(uuid: UUID): String? = find(uuid)?.lastServer
    suspend fun updateLastServer(uuid: UUID, server: String) = update(uuid) { lastServer = server }

    suspend fun findLastSeen(uuid: UUID) = find(uuid)?.lastSeen
    suspend fun updateLastSeen(uuid: UUID, lastSeen: ZonedDateTime = ZonedDateTime.now()) =
        update(uuid) { this.lastSeen = lastSeen }

    suspend fun findLastIpAddress(uuid: UUID) = find(uuid)?.lastIpAddress
    suspend fun updateLastIpAddress(uuid: UUID, address: Inet4Address) =
        update(uuid) { lastIpAddress = address }

    suspend fun findNameHistories(uuid: UUID) = find(uuid)?.nameHistories

    suspend fun addNameHistories(uuid: UUID, name: String) = update(uuid) {
        nameHistories.add(CloudPlayerNameHistoryEntity(name = name))
    }

    suspend fun updateOnDisconnect(player: StandaloneCloudPlayerImpl) {
        update(player.uuid) {
            this.lastSeen = ZonedDateTime.now()
            this.lastIpAddress = player.latestIpAddress()
            this.lastServer = player.lastServerRaw()
        }
    }

    suspend fun updateOnServerConnect(player: StandaloneCloudPlayerImpl) {
        update(player.uuid) {
            this.lastSeen = ZonedDateTime.now()
            this.lastIpAddress = player.latestIpAddress()
            this.lastServer = player.lastServerRaw()
        }
    }

    @Transactional
    protected suspend fun update(uuid: UUID, block: suspend CloudPlayerEntity.() -> Unit) =
        withContext(PlayerDatabaseScope.context) {
            cloudPlayerRepository.findByUuid(uuid)?.apply { block() }
                ?.let { cloudPlayerRepository.save(it) }
        }

    @Transactional
    protected suspend fun find(uuid: UUID) =
        withContext(PlayerDatabaseScope.context) { cloudPlayerRepository.findByUuid(uuid) }
}