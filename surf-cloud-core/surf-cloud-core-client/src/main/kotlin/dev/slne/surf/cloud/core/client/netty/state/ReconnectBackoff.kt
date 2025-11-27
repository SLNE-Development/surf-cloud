package dev.slne.surf.cloud.core.client.netty.state

import dev.slne.surf.cloud.core.client.config.ClientConfigHolder
import dev.slne.surf.cloud.core.common.config.ConfigReloadAware
import org.springframework.stereotype.Component
import org.springframework.util.backoff.ExponentialBackOff

@Component
class ReconnectBackoff(private val configHolder: ClientConfigHolder) : ConfigReloadAware {

    val backoff = ExponentialBackOff()

    init {
        applyConfig()
    }

    override fun afterReload() {
        applyConfig()
    }

    private fun applyConfig() {
        val config = configHolder.config.reconnectConfig

        backoff.initialInterval = config.minDelay
        backoff.maxInterval = config.maxDelay
        backoff.maxElapsedTime = config.maxOfflineTime
    }
}