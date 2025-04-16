package dev.slne.surf.cloud.core.common.spring

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.CloudCoreInstance.BootstrapData

interface CloudLifecycleAware {
    suspend fun onBootstrap(data: BootstrapData, timeLogger: TimeLogger) {}
    suspend fun onLoad(timeLogger: TimeLogger) {}
    suspend fun onEnable(timeLogger: TimeLogger) {}
    suspend fun afterStart(timeLogger: TimeLogger) {}
    suspend fun onDisable(timeLogger: TimeLogger) {}

    companion object {
        const val MISC_PRIORITY = 1000
        const val KTOR_SERVER_PRIORITY = 750
        const val PLUGIN_MANAGER_PRIORITY = 700
        const val NETTY_MANAGER_PRIORITY = 500
    }
}