package dev.slne.surf.cloud.core.common.player

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.server.CloudServer
import java.util.UUID

abstract class CommonCloudPlayerImpl(override val uuid: UUID): CloudPlayer {

    override val persistentDataView: PersistentPlayerDataContainerView
        get() = TODO("Not yet implemented")

    override suspend fun editPersistentData(block: suspend PersistentPlayerDataContainer.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServer(server: CloudServer): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServer(
        group: String,
        server: String
    ): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServer(group: String): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServerOrQueue(server: CloudServer): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServerOrQueue(
        group: String,
        server: String
    ): ConnectionResult {
        TODO("Not yet implemented")
    }

    override suspend fun connectToServerOrQueue(group: String): ConnectionResult {
        TODO("Not yet implemented")
    }
}