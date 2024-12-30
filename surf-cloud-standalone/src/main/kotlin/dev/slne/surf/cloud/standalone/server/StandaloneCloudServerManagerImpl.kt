package dev.slne.surf.cloud.standalone.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundRegisterServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundUnregisterServerPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ProxyServerAutoregistration
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl

@AutoService(CloudServerManager::class)
class StandaloneCloudServerManagerImpl : CommonCloudServerManagerImpl<ServerCommonCloudServer>(),
    ServerCloudServerManager {
    private val server by lazy { bean<NettyServerImpl>() }


    suspend fun getCommonStandaloneServerByUid(uid: Long) = retrieveServerById(uid) as? CommonStandaloneServer

    override suspend fun registerServer(cloudServer: ServerCommonCloudServer) {
        super.registerServer(cloudServer)
        broadcast(
            ClientboundRegisterServerPacket(
                cloudServer.uid,
                cloudServer is ServerProxyCloudServer,
                cloudServer.group,
                cloudServer.name
            )
        )

        if (standaloneConfig.useSingleProxySetup) {
            if (cloudServer is StandaloneProxyCloudServerImpl) {
                ProxyServerAutoregistration.setProxy(cloudServer)
            } else {
                ProxyServerAutoregistration.registerClient(cloudServer as StandaloneCloudServerImpl)
            }
        }
    }

    override suspend fun unregisterServer(uid: Long) {
        super.unregisterServer(uid)
        broadcast(ClientboundUnregisterServerPacket(uid))

        if (standaloneConfig.useSingleProxySetup) {
            if (serverManagerImpl.getCommonStandaloneServerByUid(uid) is StandaloneProxyCloudServerImpl) {
                ProxyServerAutoregistration.clearProxy()
            }
        }
    }

    private fun broadcast(packet: NettyPacket) {
        server.connection.broadcast(packet)
    }
}

val serverManagerImpl get() = CloudServerManager.instance as StandaloneCloudServerManagerImpl