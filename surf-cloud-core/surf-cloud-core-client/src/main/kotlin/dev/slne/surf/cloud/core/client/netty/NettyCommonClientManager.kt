package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.client.netty.state.ReconnectBackoff
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.NettyManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes

abstract class NettyCommonClientManager(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    private val configHolder: AbstractSurfCloudConfigHolder<*>,
    private val reconnectBackoff: ReconnectBackoff
) : NettyManager() {
    val nettyClient by lazy {
        ClientNettyClientImpl(
            proxy,
            platformExtension,
            configHolder,
            this,
            reconnectBackoff
        )
    }

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

    override suspend fun onEnable(timeLogger: TimeLogger) {
        super.onEnable(timeLogger)
        timeLogger.measureStep("Synchronizing Netty client") {
            ConnectionManagementScope.launch {
                nettyClient.startSynchronizeTask()
            }

            nettyClient.synchronizeFinishedSignal.awaitNext()
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