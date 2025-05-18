package dev.slne.surf.cloud.api.server.queue

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerQueue : BaseQueue<ServerQueue> {
    val serverUid: Long

    suspend fun server() = CloudServerManager.retrieveServerById(serverUid) as? CloudServer
}