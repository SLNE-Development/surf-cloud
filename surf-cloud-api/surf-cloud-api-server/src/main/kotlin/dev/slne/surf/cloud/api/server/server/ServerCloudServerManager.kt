package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerCloudServerManager : CloudServerManager {

    override suspend fun retrieveServerByCategoryAndName(category: String, name: String) =
        getServerByCategoryAndName(category, name)

    override suspend fun retrieveServerById(id: Long) = getServerById(id)
    override suspend fun retrieveServersByCategory(category: String) =
        getServersByCategory(category)

    override suspend fun retrieveServerByName(name: String) = getServerByName(name)

    suspend fun getServerById(id: Long): ServerCloudServer?
    suspend fun getServerByCategoryAndName(category: String, name: String): ServerCloudServer?
    suspend fun getServerByName(name: String): ServerCloudServer?
    suspend fun getServersByCategory(category: String): ObjectList<ServerCloudServer>

    companion object {
        val instance get() = CloudServerManager.instance as ServerCloudServerManager
    }
}

val serverServerManager get() = ServerCloudServerManager.instance