package dev.slne.surf.cloud.core.client

import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.core.client.netty.network.ClientEncryptionManager
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl

abstract class ClientCommonCloudInstance(nettyManager: NettyManager) :
    SurfCloudCoreInstance(nettyManager) {

    override suspend fun preBootstrap() {
        super.preBootstrap()
        ClientEncryptionManager.init()
    }

    override fun shutdownServer(server: CommonCloudServerImpl) {
        (nettyManager as NettyCommonClientManager).nettyClient.connection.send(
            ServerboundShutdownServerPacket(server.uid)
        )
    }
}