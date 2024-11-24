package dev.slne.surf.cloud.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestCloudServerByCategoryAndNamePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestCloudServerByIdPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestCloudServerByNamePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestCloudServersByCategory
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import it.unimi.dsi.fastutil.objects.ObjectList

@AutoService(CloudServerManager::class)
class CommonCloudServerManagerImpl : CloudServerManager {
    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun retrieveServerById(id: Long): CloudServer? =
        ServerboundRequestCloudServerByIdPacket(id).fireAndAwaitOrThrow().server

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ): CloudServer? = ServerboundRequestCloudServerByCategoryAndNamePacket(
        category,
        name
    ).fireAndAwaitOrThrow().server

    override suspend fun retrieveServerByName(name: String): CloudServer? =
        ServerboundRequestCloudServerByNamePacket(name).fireAndAwaitOrThrow().server

    override suspend fun retrieveServersByCategory(category: String): ObjectList<out CloudServer> =
        ServerboundRequestCloudServersByCategory(category).fireAndAwaitOrThrow().servers
}