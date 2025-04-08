package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.util.mutableLong2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.api.common.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectCollection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class CommonCloudServerManagerImpl<S : CommonCloudServer> : CloudServerManager {
    protected val servers = mutableLong2ObjectMapOf<S>().synchronize()
    protected val serversMutex = Mutex()

    open suspend fun registerServer(server: S) =
        serversMutex.withLock { servers[server.uid] = server }

    open suspend fun unregisterServer(uid: Long) =
        serversMutex.withLock<Unit> { servers.remove(uid) }

    override suspend fun retrieveServerById(id: Long): S? = serversMutex.withLock { servers[id] }

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

    suspend fun batchUpdateServer(update: List<CommonCloudServer>) {
        serversMutex.withLock {
            update.forEach { servers[it.uid] = it as S }
        }
    }

    override suspend fun retrieveAllServers(): ObjectCollection<S> {
        return serversMutex.withLock { servers.values.toObjectSet() }
    }
}