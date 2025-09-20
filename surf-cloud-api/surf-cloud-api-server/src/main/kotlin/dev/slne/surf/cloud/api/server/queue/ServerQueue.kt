package dev.slne.surf.cloud.api.server.queue

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerQueue : BaseQueue<ServerQueue> {
    val serverName: String

    suspend fun server() = CloudServerManager.retrieveServerByName(serverName) as? CloudServer
}