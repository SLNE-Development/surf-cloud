package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerCloudServerManager : CloudServerManager {
    
    override fun retrieveServerByCategoryAndName(
        category: String,
        name: String
    ): ServerCommonCloudServer?

    override fun retrieveServerByName(name: String): ServerCommonCloudServer?
    override fun retrieveServersInGroup(group: String): ObjectList<out ServerCommonCloudServer>
    override fun retrieveServersByCategory(category: String): ObjectList<out ServerCommonCloudServer>
    override fun retrieveAllServers(): ObjectCollection<out ServerCommonCloudServer>
    override fun retrieveServers(): ObjectCollection<out ServerCloudServer>
    override fun retrieveProxies(): ObjectCollection<out ServerProxyCloudServer>

    fun broadcast(packet: NettyPacket)

    companion object :
        ServerCloudServerManager by CloudServerManager.instance as ServerCloudServerManager
}