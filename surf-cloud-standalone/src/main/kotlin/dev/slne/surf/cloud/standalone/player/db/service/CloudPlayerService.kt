package dev.slne.surf.cloud.standalone.player.db.service

import dev.slne.surf.cloud.api.server.exposed.service.AbstractExposedDAOService
import dev.slne.surf.cloud.core.common.coroutines.PlayerDatabaseScope
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.db.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.CloudPlayerNameHistories
import dev.slne.surf.cloud.standalone.player.db.CloudPlayers
import org.jetbrains.exposed.sql.insert
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.ZonedDateTime
import java.util.*


@Service
class CloudPlayerService : AbstractExposedDAOService<UUID, CloudPlayerEntity>(
    { maximumSize(1000) },
    PlayerDatabaseScope.context
) {

    suspend fun findLastServer(uuid: UUID): String? = find(uuid)?.lastServer
    suspend fun updateLastServer(uuid: UUID, server: String) = update(uuid) { lastServer = server }

    suspend fun findLastSeen(uuid: UUID) = find(uuid)?.lastSeen
    suspend fun updateLastSeen(uuid: UUID, lastSeen: ZonedDateTime = ZonedDateTime.now()) =
        update(uuid) { this.lastSeen = lastSeen }

    suspend fun findLastIpAddress(uuid: UUID) = find(uuid)?.lastIpAddress
    suspend fun updateLastIpAddress(uuid: UUID, address: InetAddress) =
        update(uuid) { lastIpAddress = address }

    suspend fun findNameHistories(uuid: UUID) = find(uuid)?.nameHistories
    suspend fun addNameHistories(uuid: UUID, name: String) = update(uuid) {
        CloudPlayerNameHistories.insert {
            it[this.name] = name
            it[this.player] = this@update.id
        }
    }

    suspend fun updateOnDisconnect(player: StandaloneCloudPlayerImpl) {
        update(player.uuid) {
            this.lastSeen = ZonedDateTime.now()
            this.lastIpAddress = player.latestIpAddress()
            this.lastServer = player.lastServerRaw()
        }

        evict(player.uuid)
    }

    override suspend fun load(key: UUID): CloudPlayerEntity? {
        return CloudPlayerEntity.find { CloudPlayers.uuid eq key }.singleOrNull()
    }
}