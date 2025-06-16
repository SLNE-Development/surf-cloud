package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerCloudServerManager : CloudServerManager {

    override suspend fun retrieveServerById(id: Long): CommonCloudServer?

    override suspend fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ): ServerCommonCloudServer?

    override suspend fun retrieveServerByName(name: String): ServerCommonCloudServer?
    override suspend fun retrieveServersInGroup(group: String): ObjectList<out ServerCommonCloudServer>
    override suspend fun retrieveServersByCategory(category: String): ObjectList<out ServerCommonCloudServer>
    override suspend fun retrieveAllServers(): ObjectCollection<out ServerCommonCloudServer>
    override suspend fun retrieveServers(): ObjectCollection<out ServerCloudServer>
    override suspend fun retrieveProxies(): ObjectCollection<out ServerProxyCloudServer>

    fun broadcast(packet: NettyPacket)

    companion object :
        ServerCloudServerManager by CloudServerManager.instance as ServerCloudServerManager
}