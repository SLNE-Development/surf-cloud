package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.util.requiredService
import org.jetbrains.annotations.ApiStatus.NonExtendable

@NonExtendable
interface CloudServerManager {

    suspend fun retrieveServerById(id: Long): CloudServer?
    suspend fun retrieveServerByCategoryAndName(category: String, name: String): CloudServer?
    suspend fun retrieveServerByName(name: String): CloudServer?
    suspend fun retrieveServersByCategory(category: String): List<CloudServer>

    companion object {
        val instance = requiredService<CloudServerManager>()
    }
}
val serverManager get() = CloudServerManager.instance