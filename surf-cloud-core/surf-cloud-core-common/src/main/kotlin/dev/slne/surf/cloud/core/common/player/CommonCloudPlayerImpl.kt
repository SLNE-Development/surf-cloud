package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import java.time.ZonedDateTime
import java.util.*

abstract class CommonCloudPlayerImpl(uuid: UUID, override val name: String) :
    CommonOfflineCloudPlayerImpl(uuid), CloudPlayer {

    override suspend fun connectToServer(
        group: String,
        server: String
    ): ConnectionResultEnum = CloudServerManager.retrieveServerByCategoryAndName(group, server)
        ?.let {
            it as? CloudServer ?: return ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY
        }
        ?.let { connectToServer(it) }
        ?: ConnectionResultEnum.SERVER_NOT_FOUND(server)

    override suspend fun connectToServer(group: String): ConnectionResultEnum =
        CloudServerManager.retrieveServersByCategory(group).asSequence()
            .filterIsInstance<CloudServer>()
            .filter { it.hasEmptySlots() }
            .also { if (it.none()) return ConnectionResultEnum.CATEGORY_FULL }
            .minBy { it.currentPlayerCount }
            .let { connectToServer(it) }

    override suspend fun connectToServerOrQueue(
        group: String,
        server: String,
        sendQueuedMessage: Boolean
    ): ConnectionResultEnum = CloudServerManager.retrieveServerByCategoryAndName(group, server)
        ?.let {
            it as? CloudServer ?: return ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY
        }
        ?.let { connectToServerOrQueue(it, sendQueuedMessage) }
        ?: ConnectionResultEnum.SERVER_NOT_FOUND(server)

    override suspend fun <R> getLuckpermsMetaData(
        key: String,
        transformer: (String) -> R
    ): R? = getLuckpermsMetaData(key)?.let(transformer)

    override suspend fun lastSeen(): ZonedDateTime? {
        return ZonedDateTime.now()
    }

    override suspend fun playedBefore(): Boolean {
        return true
    }

    override suspend fun lastServer(): CloudServer {
        return currentServer()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommonCloudPlayerImpl) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}