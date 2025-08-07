package dev.slne.surf.cloud.standalone.config

import org.springframework.stereotype.Component

@Component
interface ConfigReloadAware {
    fun beforeReload() = Unit
    fun afterReload() = Unit
}