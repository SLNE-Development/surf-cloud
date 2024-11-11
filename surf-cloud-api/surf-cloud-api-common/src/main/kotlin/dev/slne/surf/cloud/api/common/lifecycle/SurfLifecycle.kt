package dev.slne.surf.cloud.api.common.lifecycle

import org.jetbrains.annotations.ApiStatus
import org.springframework.stereotype.Component

@ApiStatus.OverrideOnly
@Component
interface SurfLifecycle {
    fun onLoad() {
    }

    fun onEnable() {
    }

    fun onDisable() {
    }

    fun onReload() {
        onDisable()
        onEnable()
    }
}
