package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.coroutines.CloudConnectionVerificationScope
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

        CloudConnectionVerificationScope.launch {
            while (isActive) {
                delay(5.minutes)
                if (!nettyClient.connected) {
                    logger().atSevere().log("Netty client is not connected, rebooting...")
                    nettyClient.platformExtension.restart()
                }
            }
        }
    }

    override suspend fun onEnable(timeLogger: TimeLogger) {
        super.onEnable(timeLogger)
        timeLogger.measureStep("Synchronizing Netty client") {
            nettyClient.startSynchronizeTask()
            nettyClient.synchronizeCallback.await()
        }
    }

    override suspend fun afterStart(timeLogger: TimeLogger) {
        super.afterStart(timeLogger)
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Stop Netty client") {
            nettyClient.stop()
        }
    }
}