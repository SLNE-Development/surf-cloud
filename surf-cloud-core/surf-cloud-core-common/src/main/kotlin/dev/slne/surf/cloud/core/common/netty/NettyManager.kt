package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware

abstract class NettyManager : CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Blocking player connections") {
            blockPlayerConnections()
        }
    }

    override suspend fun afterStart(timeLogger: TimeLogger) {
        timeLogger.measureStep("Unblocking player connections") {
            unblockPlayerConnections()
        }
    }

    abstract fun blockPlayerConnections()
    abstract fun unblockPlayerConnections()
}