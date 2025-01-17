package dev.slne.surf.cloud.api.server.plugin.coroutine

import dev.slne.surf.cloud.api.common.util.InternalApi
import dev.slne.surf.cloud.api.common.util.requiredService
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin

@InternalApi
interface CoroutineManager {
    companion object {
       internal val instance = requiredService<CoroutineManager>()
    }

    fun getCoroutineSession(plugin: StandalonePlugin): CoroutineSession
    fun setupCoroutineSession(plugin: StandalonePlugin)
    fun disable(plugin: StandalonePlugin)
}