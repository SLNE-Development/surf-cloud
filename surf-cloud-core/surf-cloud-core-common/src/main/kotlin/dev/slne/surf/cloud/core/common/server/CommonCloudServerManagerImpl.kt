package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.api.common.util.*
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component

abstract class CommonCloudServerManagerImpl<CommonServer : CommonCloudServer> : CloudServerManager {
    protected val servers = mutableLong2ObjectMapOf<CommonServer>().synchronize()
    protected val serversMutex = Mutex()

    open suspend fun registerServer(server: CommonServer) =
        serversMutex.withLock { servers[server.uid] = server }

    open suspend fun unregisterServer(uid: Long): CommonServer? =
        serversMutex.withLock { servers.remove(uid) }

    fun getServerByIdUnsafe(uid: Long): CommonServer? = servers[uid]

    override suspend fun retrieveServerById(id: Long): CommonServer? = serversMutex.withLock { servers[id] }

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ) = serversMutex.withLock {
        servers.values.asSequence()
            .filter { it.isInGroup(category) && it.name.equals(name, true) }
            .minByOrNull { it.currentPlayerCount }
    }

    override suspend fun retrieveServerByName(name: String) = serversMutex.withLock {
        servers.values.asSequence()
            .filter { it.name.equals(name, true) }
            .minByOrNull { it.currentPlayerCount }
    }

    override fun getServerByNameUnsafe(name: String): CloudServer? =
        servers.values.asSequence()
            .filter { it.name.equals(name, ignoreCase = true) }
            .minByOrNull { it.currentPlayerCount } as? CloudServer

    override suspend fun retrieveServersInGroup(group: String): ObjectList<out CommonServer> =
        serversMutex.withLock {
            servers.values.filterTo(mutableObjectListOf()) { it.isInGroup(group) }
        }


    @InternalApi
    override fun existsServerGroup(name: String): Boolean =
        servers.values.any { it.group.equals(name, ignoreCase = true) }

    override suspend fun retrieveServersByCategory(category: String) = serversMutex.withLock {
        servers.values.filterTo(mutableObjectListOf()) { it.group.equals(category, true) }
    }

    suspend fun batchUpdateServer(update: List<CommonCloudServer>) {
        serversMutex.withLock {
            update.forEach { servers[it.uid] = it as CommonServer }
        }
    }

    override suspend fun retrieveAllServers(): ObjectCollection<CommonServer> {
        return serversMutex.withLock { servers.values.toObjectSet() }
    }

    override suspend fun retrieveServers(): ObjectCollection<out CloudServer> {
        return serversMutex.withLock { servers.values.filterIsInstanceTo(mutableObjectSetOf<CloudServer>()) }.freeze()
    }

    override suspend fun retrieveProxies(): ObjectCollection<out ProxyCloudServer> {
        return serversMutex.withLock { servers.values.filterIsInstanceTo(mutableObjectSetOf<ProxyCloudServer>()) }.freeze()
    }

    override suspend fun broadcastToGroup(group: String, message: Component, permission: String?, playSound: Boolean) =
        serversMutex.withLock {
            servers.values.filter { it.isInGroup(group) }.filterIsInstance<CloudServer>()
        }.forEach { it.broadcast(message, permission, playSound) }


    override suspend fun broadcast(message: Component, permission: String?, playSound: Boolean) = serversMutex.withLock {
        servers.values.filterIsInstance<CloudServer>()
    }.forEach { it.broadcast(message, permission, playSound) }
}