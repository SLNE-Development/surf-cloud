package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl: CloudServerManager {
    override suspend fun retrieveServerById(id: Long): CloudServer? {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ): CloudServer? {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveServerByName(name: String): CloudServer? {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveServersByCategory(category: String): List<CloudServer> {
        TODO("Not yet implemented")
    }

    fun getServerById(id: Long): CloudServer? {
        TODO("Not yet implemented")
    }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl