package dev.slne.surf.cloud.standalone.player.db.service

import dev.slne.surf.cloud.core.common.coroutines.PlayerDatabaseScope
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.db.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.CloudPlayerNameHistories
import dev.slne.surf.cloud.standalone.player.db.CloudPlayers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.ZonedDateTime
import java.util.*


@Service
class CloudPlayerService {

    @Cacheable("lastServer")
    suspend fun findLastServer(uuid: UUID): String? = find(uuid) { lastServer }

    @CachePut("lastServer")
    suspend fun updateLastServer(uuid: UUID, server: String) = update(uuid) { lastServer = server }

    @Cacheable("lastSeen")
    suspend fun findLastSeen(uuid: UUID) = find(uuid) { lastSeen }

    @CachePut("lastSeen")
    suspend fun updateLastSeen(uuid: UUID, lastSeen: ZonedDateTime = ZonedDateTime.now()) =
        update(uuid) { this.lastSeen = lastSeen }

    @Cacheable("lastIpAddress")
    suspend fun findLastIpAddress(uuid: UUID) = find(uuid) { lastIpAddress }

    @CachePut("lastIpAddress")
    suspend fun updateLastIpAddress(uuid: UUID, address: InetAddress) =
        update(uuid) { lastIpAddress = address }

    @Cacheable("nameHistories")
    suspend fun findNameHistories(uuid: UUID) = find(uuid) { nameHistories }

    @CachePut("nameHistories")
    suspend fun addNameHistories(uuid: UUID, name: String) = update(uuid) {
        CloudPlayerNameHistories.insert {
            it[this.name] = name
            it[this.player] = this@update.id
        }
    }?.nameHistories


    @CacheEvict("lastServer", "lastSeen", "lastIpAddress")
    suspend fun updateOnDisconnect(player: StandaloneCloudPlayerImpl) {
        val lastIpAddress = player.latestIpAddress()
        val lastServer = player.lastServerRaw()

        update(player.uuid) {
            this.lastSeen = ZonedDateTime.now()
            this.lastIpAddress = lastIpAddress
            this.lastServer = lastServer
        }
    }

    private suspend inline fun <T> find(
        uuid: UUID,
        crossinline selector: CloudPlayerEntity.() -> T?
    ): T? = newSuspendedTransaction(PlayerDatabaseScope.context) {
        CloudPlayerEntity.find { CloudPlayers.uuid eq uuid }
            .singleOrNull()
            ?.selector()
    }

    private suspend inline fun update(
        uuid: UUID,
        noinline updateAction: CloudPlayerEntity.() -> Unit
    ) = newSuspendedTransaction(PlayerDatabaseScope.context) {
        CloudPlayerEntity.findSingleByAndUpdate(CloudPlayers.uuid eq uuid, updateAction)
    }
}