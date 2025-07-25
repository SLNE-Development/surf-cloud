package dev.slne.surf.cloud.standalone.player.db.exposed

import dev.slne.surf.cloud.api.common.player.name.NameHistoryFactory
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.exposed.service.AbstractExposedDAOService
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.cloud.core.common.player.playtime.PlaytimeEntry
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.name.create
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
@CoroutineTransactional
class CloudPlayerService : AbstractExposedDAOService<UUID, CloudPlayerEntity>({
    maximumSize(1000)
}) {
    override suspend fun load(key: UUID): CloudPlayerEntity? =
        CloudPlayerEntity.find { CloudPlayerTable.uuid eq key }.singleOrNull()

    override suspend fun create(key: UUID) = CloudPlayerEntity.new {
        uuid = key
    }

    suspend fun findNameHistories(uuid: UUID) =
        find(uuid) { NameHistoryFactory.instance.create(nameHistories) }
            ?: NameHistoryFactory.instance.empty()

    suspend fun findLastServer(uuid: UUID) = find(uuid) { lastServer }
    suspend fun findLastSeen(uuid: UUID) = find(uuid) { lastSeen }
    suspend fun findFirstSeen(uuid: UUID) = find(uuid) { createdAt }
    suspend fun findLastIpAddress(uuid: UUID) = find(uuid) { lastIpAddress }

    suspend fun updateOnDisconnect(player: StandaloneCloudPlayerImpl, oldServer: Long?) {
        update(player.uuid) {
            lastSeen = ZonedDateTime.now()
            lastIpAddress = player.ip
            if (oldServer != null) {
                serverManagerImpl.retrieveServerById(oldServer)?.name?.let {
                    lastServer = it
                }
            }
        }
    }

    suspend fun updateOnServerConnect(player: StandaloneCloudPlayerImpl) {
        update(player.uuid, createIfMissing = true) {
            lastSeen = ZonedDateTime.now()
            lastIpAddress = player.ip
            player.server?.let { lastServer = it.name }

            val latestName = nameHistories.minByOrNull { it.createdAt }
            val currentName = player.name()
            if (latestName == null || latestName.name != currentName) {
                CloudPlayerNameHistoryEntity.new {
                    name = currentName
                    this.player = this@update
                }
            }
        }
    }

    suspend fun createPlaytimeSession(uuid: UUID, serverName: String, category: String): Long {
        var id: Long? = null
        update(uuid, createIfMissing = true) {
            id = CloudPlayerPlaytimesEntity.new {
                this.serverName = serverName
                this.category = category
                this.player = this@update
            }.id.value
        }

        return id ?: error("Failed to create playtime session for player $uuid")
    }

    suspend fun updatePlaytimeInSession(uuid: UUID, playtimeId: Long, playtimeSeconds: Long) =
        CloudPlayerPlaytimesEntity.findByIdAndUpdate(playtimeId) {
            it.durationSeconds = playtimeSeconds
        }

    suspend fun createIfNotExists(uuid: UUID) {
        if (find(uuid) == null) {
            create(uuid)
        }
    }

    suspend fun loadPlaytimeEntries(uuid: UUID) =
        find(uuid)?.playtimes?.mapTo(mutableObjectListOf()) {
            PlaytimeEntry(
                id = it.id.value,
                category = it.category,
                server = it.serverName,
                durationSeconds = it.durationSeconds,
                createdAt = it.createdAt,
            )
        } ?: mutableObjectListOf()
}