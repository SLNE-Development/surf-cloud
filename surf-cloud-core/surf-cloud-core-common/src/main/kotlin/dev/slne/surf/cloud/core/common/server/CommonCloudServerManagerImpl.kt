package dev.slne.surf.cloud.core.common.server

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.common.util.freeze
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component

abstract class CommonCloudServerManagerImpl<CommonServer : CommonCloudServer> : CloudServerManager {
    protected val serverCache = Caffeine.newBuilder()
        .build<String, CommonServer>()

    open fun registerServer(server: CommonServer) = serverCache.put(server.name, server)
    open fun unregisterServer(name: String): CommonServer? = serverCache.asMap().remove(name)

    override fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ) = serverCache.asMap().values.asSequence()
        .filter { it.isInGroup(category) && it.name.equals(name, true) }
        .singleOrNull()

    override fun retrieveServerByName(name: String) =
        serverCache.asMap().values.asSequence()
            .filter { it.name.equals(name, true) }
            .singleOrNull()

    override fun retrieveServersInGroup(group: String): ObjectList<out CommonServer> =
        serverCache.asMap().values.filterTo(mutableObjectListOf()) { it.isInGroup(group) }

    @InternalApi
    override fun existsServerGroup(name: String): Boolean =
        serverCache.asMap().values.any { it.group.equals(name, ignoreCase = true) }

    override fun retrieveServersByCategory(category: String) =
        serverCache.asMap().values.filterTo(mutableObjectListOf()) {
            it.group.equals(category, true)
        }


    fun batchUpdateServer(update: List<CommonCloudServer>) {
        serverCache.putAll(update.associateBy { it.name } as Map<String, CommonServer>)
    }

    override fun retrieveAllServers(): ObjectCollection<CommonServer> {
        return serverCache.asMap().values.toObjectSet()
    }

    override fun retrieveServers(): ObjectCollection<out CloudServer> {
        return serverCache.asMap().values.filterIsInstanceTo(mutableObjectSetOf<CloudServer>())
            .freeze()
    }

    override fun retrieveProxies(): ObjectCollection<out ProxyCloudServer> {
        return serverCache.asMap().values.filterIsInstanceTo(mutableObjectSetOf<ProxyCloudServer>())
            .freeze()
    }

    override suspend fun broadcastToGroup(
        group: String,
        message: Component,
        permission: String?,
        playSound: Boolean
    ) = serverCache.asMap().values.filter { it.isInGroup(group) }.filterIsInstance<CloudServer>()
        .forEach { it.broadcast(message, permission, playSound) }


    override suspend fun broadcast(message: Component, permission: String?, playSound: Boolean) =
        serverCache.asMap().values.filterIsInstance<CloudServer>()
            .forEach { it.broadcast(message, permission, playSound) }
}