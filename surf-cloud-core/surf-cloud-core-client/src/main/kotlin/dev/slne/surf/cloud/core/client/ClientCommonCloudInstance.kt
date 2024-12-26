package dev.slne.surf.cloud.core.client

import dev.slne.surf.cloud.core.client.netty.network.ClientEncryptionManager
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.netty.NettyManager

abstract class ClientCommonCloudInstance(nettyManager: NettyManager) :
    SurfCloudCoreInstance(nettyManager) {

    override suspend fun preBootstrap() {
        super.preBootstrap()
        ClientEncryptionManager.init()
    }
}