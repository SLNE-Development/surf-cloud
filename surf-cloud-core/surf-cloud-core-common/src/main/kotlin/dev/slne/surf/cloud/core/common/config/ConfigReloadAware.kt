package dev.slne.surf.cloud.core.common.config

import org.springframework.stereotype.Component

@Component
interface ConfigReloadAware {
    fun beforeReload() = Unit
    fun afterReload() = Unit

    fun beforeSave() = Unit
}