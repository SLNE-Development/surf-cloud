package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.mutableLong2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl : ServerCloudServerManager {
    private val servers = mutableLong2ObjectMapOf<StandaloneServerImplCommon>().synchronize()
    private val serversMutex = Mutex()

    override suspend fun retrieveServerById(id: Long): StandaloneServerImplCommon? =
        serversMutex.withLock { servers[id] }

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ) = serversMutex.withLock {
        servers.values.asSequence()
            .filter { it.group == category && it.name == name }
            .minByOrNull { it.currentPlayerCount }
    }

    override suspend fun retrieveServerByName(name: String) = serversMutex.withLock {
        servers.values.asSequence()
            .filter { it.name == name }
            .minByOrNull { it.currentPlayerCount }
    }

    override suspend fun retrieveServersByCategory(category: String) = serversMutex.withLock {
        servers.values.filterTo(mutableObjectListOf()) { it.group == category }
    }

    suspend fun registerServer(server: StandaloneServerImplCommon) = serversMutex.withLock {
        servers[server.uid] = server
    }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl