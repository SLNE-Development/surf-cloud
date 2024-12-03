package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.serverManager
import java.util.*

abstract class CommonCloudPlayerImpl(override val uuid: UUID) : CloudPlayer {

    override val persistentDataView: PersistentPlayerDataContainerView
        get() = TODO("Not yet implemented")

    override suspend fun editPersistentData(block: suspend PersistentPlayerDataContainer.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServer(
        group: String,
        server: String
    ): ConnectionResult = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let { it as? CloudServer ?: return (ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY to null) }
        ?.let { connectToServer(it) }
        ?: (ConnectionResultEnum.SERVER_NOT_FOUND to null)

    override suspend fun connectToServer(group: String): ConnectionResult =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filterIsInstance<CloudServer>()
            .filter { it.emptySlots > 0 }
            .minBy { it.currentPlayerCount }
            .let { connectToServer(it) }

    override suspend fun connectToServerOrQueue(
        group: String,
        server: String
    ): ConnectionResult = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let { it as? CloudServer ?: return (ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY to null) }
        ?.let { connectToServerOrQueue(it) }
        ?: (ConnectionResultEnum.SERVER_NOT_FOUND to null)

    override suspend fun connectToServerOrQueue(group: String): ConnectionResult =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filterIsInstance<CloudServer>()
            .filter { it.emptySlots > 0 }
            .minBy { it.currentPlayerCount }
            .let { connectToServerOrQueue(it) }
}