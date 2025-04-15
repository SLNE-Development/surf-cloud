package dev.slne.surf.cloud.core.client

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.common.util.measure
import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.core.client.netty.network.ClientEncryptionManager
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import org.springframework.util.StopWatch

abstract class ClientCommonCloudInstance(nettyManager: NettyManager) :
    CloudCoreInstance(nettyManager) {

    override suspend fun preBootstrap(timeLogger: TimeLogger) {
        timeLogger.measureStep("Setup client encryption manager") {
            ClientEncryptionManager.init()
        }
    }

    override fun shutdownServer(server: CommonCloudServerImpl) {
        (nettyManager as NettyCommonClientManager).nettyClient.connection.send(
            ServerboundShutdownServerPacket(server.uid)
        )
    }
}