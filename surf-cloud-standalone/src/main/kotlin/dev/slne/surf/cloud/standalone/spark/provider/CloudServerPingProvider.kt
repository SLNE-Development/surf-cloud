package dev.slne.surf.cloud.standalone.spark.provider

import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import me.lucko.spark.common.monitor.ping.PlayerPingProvider

object CloudServerPingProvider : PlayerPingProvider {
    override fun poll(): Map<String, Int> {
        return serverManagerImpl.getPingData()
    }
}