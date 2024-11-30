package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
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
    ) = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let { connectToServer(it) }
        ?: ConnectionResult.SERVER_NOT_FOUND

    override suspend fun connectToServer(group: String) =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filter { it.emptySlots > 0 }
            .minBy { it.currentPlayerCount }
            ?.let { connectToServer(it) }
            ?: ConnectionResult.SERVER_NOT_FOUND

    override suspend fun connectToServerOrQueue(
        group: String,
        server: String
    ) = serverManager.retrieveServerByCategoryAndName(group, server)
        ?.let { connectToServerOrQueue(it) }
        ?: ConnectionResult.SERVER_NOT_FOUND

    override suspend fun connectToServerOrQueue(group: String) =
        serverManager.retrieveServersByCategory(group).asSequence()
            .filter { it.emptySlots > 0 }
            .minBy { it.currentPlayerCount }
            ?.let { connectToServerOrQueue(it) }
            ?: ConnectionResult.SERVER_NOT_FOUND
}