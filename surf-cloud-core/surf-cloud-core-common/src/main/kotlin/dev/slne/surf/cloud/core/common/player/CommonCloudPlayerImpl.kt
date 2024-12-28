package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.serverManager
import java.util.*

abstract class CommonCloudPlayerImpl(override val uuid: UUID) : CloudPlayer {

    override suspend fun connectToServer(
        group: String,
        server: String
    ): ConnectionResult = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let {
            it as? CloudServer ?: return (ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY to null)
        }
        ?.let { connectToServer(it) }
        ?: (ConnectionResultEnum.SERVER_NOT_FOUND to null)

    override suspend fun connectToServer(group: String): ConnectionResult =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filterIsInstance<CloudServer>()
            .filter { it.hasEmptySlots() }
            .also { if (it.none()) return (ConnectionResultEnum.CATEGORY_FULL to null) }
            .minBy { it.currentPlayerCount }
            .let { connectToServer(it) }

    override suspend fun connectToServerOrQueue(
        group: String,
        server: String
    ): ConnectionResult = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let {
            it as? CloudServer ?: return (ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY to null)
        }
        ?.let { connectToServerOrQueue(it) }
        ?: (ConnectionResultEnum.SERVER_NOT_FOUND to null)

    override suspend fun connectToServerOrQueue(group: String): ConnectionResult =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filterIsInstance<CloudServer>()
//            .filter { it.emptySlots > 0 }
            .minBy { it.currentPlayerCount } // also check player count in queue / maybe do it completely different - a group queue?
            .let { connectToServerOrQueue(it) }

    override suspend fun <R> getLuckpermsMetaData(
        key: String,
        transformer: (String) -> R
    ): R? = getLuckpermsMetaData(key)?.let(transformer)

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