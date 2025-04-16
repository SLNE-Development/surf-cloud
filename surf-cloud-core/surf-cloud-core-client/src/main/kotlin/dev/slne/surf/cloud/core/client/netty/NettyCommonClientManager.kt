package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.netty.NettyManager
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes

abstract class NettyCommonClientManager(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : NettyManager() {
    val nettyClient by lazy { ClientNettyClientImpl(proxy, platformExtension) }

    override suspend fun onBootstrap(
        data: CloudCoreInstance.BootstrapData,
        timeLogger: TimeLogger
    ) {
        withTimeout(1.minutes) {
            timeLogger.measureStep("Bootstrap Netty client") {
                nettyClient.bootstrap()
            }
        }
    }

    override suspend fun afterStart(timeLogger: TimeLogger) {
        timeLogger.measureStep("Finalize Netty client") {
            nettyClient.finalize()
        }

        super.afterStart(timeLogger)
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Stop Netty client") {
            nettyClient.stop()
        }
    }
}