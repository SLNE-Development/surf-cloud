package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.server.CloudServerManager
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerCloudServerManager : CloudServerManager {

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ): ServerCommonCloudServer?

    override suspend fun retrieveServerById(id: Long): ServerCommonCloudServer?
    override suspend fun retrieveServerByName(name: String): ServerCommonCloudServer?
    override suspend fun retrieveServersByCategory(category: String): ObjectList<out ServerCommonCloudServer>

    companion object {
        val instance get() = CloudServerManager.instance as ServerCloudServerManager
    }
}

val serverServerManager get() = ServerCloudServerManager.instance