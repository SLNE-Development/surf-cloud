package dev.slne.surf.cloud.standalone.player.db.service

import dev.slne.surf.cloud.standalone.player.db.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.CloudPlayers
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    private suspend fun <T> find(
        uuid: UUID,
        selector: CloudPlayerEntity.() -> T?
    ): T? = newSuspendedTransaction(Dispatchers.IO) {
        CloudPlayerEntity.find { CloudPlayers.uuid eq uuid }
            .singleOrNull()
            ?.selector()
    }

    private suspend fun update(
        uuid: UUID,
        updateAction: CloudPlayerEntity.() -> Unit
    ) = newSuspendedTransaction(Dispatchers.IO) {
        CloudPlayerEntity.findSingleByAndUpdate(CloudPlayers.uuid eq uuid, updateAction)
    }
}