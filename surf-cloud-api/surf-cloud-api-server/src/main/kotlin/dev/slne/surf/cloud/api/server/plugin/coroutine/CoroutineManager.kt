package dev.slne.surf.cloud.api.server.plugin.coroutine

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.surfapi.core.api.util.requiredService

@InternalApi
interface CoroutineManager {
    companion object {
       internal val instance = requiredService<CoroutineManager>()
    }

    fun getCoroutineSession(plugin: StandalonePlugin): CoroutineSession
    fun setupCoroutineSession(plugin: StandalonePlugin)
    fun disable(plugin: StandalonePlugin)
}