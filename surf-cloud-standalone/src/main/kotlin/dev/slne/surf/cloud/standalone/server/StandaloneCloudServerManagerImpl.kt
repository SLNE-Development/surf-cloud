package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.mutableLong2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl : ServerCloudServerManager {
    private val log = logger()
    private val servers = mutableLong2ObjectMapOf<ServerCloudServer>().synchronize()
    private val serversMutex = Mutex()

    override suspend fun getServerById(id: Long): ServerCloudServer? =
        serversMutex.withLock { servers[id] }

    override suspend fun getServerByCategoryAndName(
        category: String,
        name: String
    ): ServerCloudServer? = serversMutex.withLock {
        servers.values.find { it.group == category && it.name == name }
    }

    override suspend fun getServerByName(name: String): ServerCloudServer? = serversMutex.withLock {
        servers.values.find { it.name == name }
    }

    override suspend fun getServersByCategory(category: String): ObjectList<ServerCloudServer> =
        serversMutex.withLock {
            servers.values.filterTo(mutableObjectListOf()) { it.group == category }
        }

    fun getServerByUid(uid: Long): ServerCloudServer? = servers[uid]

    suspend fun registerServer(server: StandaloneServerImpl) = serversMutex.withLock {
        servers[server.uid] = server
    }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl